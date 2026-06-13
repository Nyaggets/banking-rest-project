import { URL_BASE, API_BASE, cards, client, getData, processResponse, showClientLogin } from '/js/utils/sharedData.js'
import { formatAmount, showToast, formatPhoneOrCard, formatCardOption, showSpinner } from '/js/utils/sharedFunctions.js'
const DESCRIPTION_MAX = 50

showSpinner()
showClientLogin()
const url = window.location.search
const search = new URLSearchParams(url)
let urlSenderCard
if (search.has('from')) 
    urlSenderCard = await getData(`${API_BASE}/cards/${search.get('from')}`)

const fillCardSelect = (selectEl, excludedId) => {
    selectEl.innerHTML = ''
    const selectedId = selectEl.value
    let index = 0
    cards.forEach((card) => {
        if (excludedId && card.id == excludedId) 
            return
        selectEl[index++] = new Option(formatCardOption(card), card.id, false, selectedId && card.id == selectedId)
    })
}
//отображение поля ввода в зависимости от типа операции
const senderSelect = document.getElementById('sender-cards-select')
const internalRec = document.querySelector('.internal-rec')
const internalSelect = document.querySelector('.receiver-cards-select')
const externalInput = document.querySelector('.external-rec')
const type = search.get('type')
if (!search.has('type') || type == 'INTERNAL') {
    externalInput.remove()
    internalRec.hidden = false

    internalSelect.addEventListener('change', () => fillCardSelect(senderSelect, internalSelect.value))
    senderSelect.addEventListener('change', () => fillCardSelect(internalSelect, senderSelect.value))

    const senderSelectedId = urlSenderCard?.id ?? cards[0]?.id
    fillCardSelect(internalSelect, senderSelectedId)
    fillCardSelect(senderSelect, internalSelect.value)   
}
else {
    internalRec.remove()
    externalInput.hidden = false
    fillCardSelect(senderSelect)
}

//отображение имени получателя
const receiverInput = document.getElementById('receiver')
const recipientNameEl = document.getElementById('receiver-name')
const errorElement = document.querySelector(`[error-for='receiver']`)
const clearField = (field) => {
    field.innerText = ''
    field.hidden = true
}
receiverInput.addEventListener('input', async () => {
    receiverInput.value = formatPhoneOrCard(receiverInput.value)
    recipientNameEl.innerHTML = ''
    const identifier = receiverInput.value.trim().replace(/[\s+]/g, '')
    if (identifier.length < 11) {
        clearField(errorElement)
        recipientNameEl.hidden = true
        return
    }

    const response = await fetch(`${API_BASE}/cards/owner?identifier=${identifier}`)
    if (response.ok) {
        clearField(errorElement)
        const { fullName } = await response.json()
        recipientNameEl.innerText = fullName
        recipientNameEl.hidden = false
    } 
    else if (response.status == 404) {
        clearField(recipientNameEl)
        processResponse(response)
    }
    else {
        errorElement.innerText = 'Получатель не найден'
        errorElement.hidden = false
    }
})

const descriptionInput = document.getElementById('description')
const inputCount = document.getElementById('description-size')
const updateCount = () => inputCount.innerText = `${DESCRIPTION_MAX - descriptionInput.value.length}/${DESCRIPTION_MAX}`
updateCount() 
descriptionInput.addEventListener('input', () => {
    updateCount()
    if (descriptionInput.value.length < DESCRIPTION_MAX) 
        inputCount.classList.remove('error-msg')
    else 
        inputCount.classList.add('error-msg')
    
})

//расчет комиссии
const transferBtn = document.getElementById('transfer-btn')
const calcCommission = async (amountInputEl) => {
    const cleanedAmount = amountInputEl.value.replace(' ', '').replace('.', ',')
    const commissionEl = document.getElementById('commission-amount')
    const response = await fetch(`${API_BASE}/cards/${senderSelect.value}/transactions/commission?amount=${cleanedAmount}`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json;charset=utf-8'
        },
    })
    amountInputEl.value = formatAmount(cleanedAmount)
    if (response.ok) {
        const commission = await response.text()
        const commissionFormatted = formatAmount(commission)
        commissionEl.innerText = `Комиссия: ${commissionFormatted}₽`
        const totalAmount = parseFloat(cleanedAmount) + parseFloat(commission)
        const totalAmountFormatted = formatAmount(totalAmount)
        transferBtn.innerText = `Перевести ${totalAmountFormatted} ${totalAmountFormatted ? '₽' : ''}`
    }
}

//форматирование суммы
const amountInput = document.getElementById('amount')
amountInput.addEventListener('input', async () => {
    if (parseFloat(amountInput.value) >= 100000)
        calcCommission(amountInput)
    else {
        const amountFormatted = formatAmount(amountInput.value)
        amountInput.value = amountFormatted
        transferBtn.innerText = `Перевести ${amountFormatted} ${amountFormatted ? '₽' : ''}`
    }
})
const amountBtns = document.querySelectorAll('.amount-btn')
amountBtns.forEach(btn => {
    btn.addEventListener('click', () => {
        amountInput.value = btn.innerText.replace(/[^\d\s\,]/g, '')
        transferBtn.innerText = `Перевести ${btn.innerText}`
    })
})

document.getElementById('transfer-form').addEventListener('submit', async (e) => {
    e.preventDefault()
    const errors = document.querySelectorAll('.error-msg')
    errors.forEach(elem => {
        elem.hidden = true
        elem.textContent = ''
    })
    const response = await fetch(`${API_BASE}/cards/${senderSelect.value}/transactions/transfer`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json;charset=utf-8'
        },
        body: JSON.stringify({
            clientCardId: senderSelect.value,
            counterpartyCardIdentifier: receiverInput.value.replace(/[\s\*]/g, ''), 
            amount: amountInput.value.replace(/\s/g, '').replace(/\./g, ','),
            description: descriptionInput.value
        })
    })
    if (response.ok) {
        const { operationId } = await response.json()
        const detailsUrl = `${URL_BASE}/transaction?operationId=${operationId}`
        showToast('Перевод отправлен', `<a href='${detailsUrl}' class='link'>Детали перевода</a>`)
        setTimeout(() => window.location.assign('/main'), 1600)
    }
    else 
        processResponse(response)    
})