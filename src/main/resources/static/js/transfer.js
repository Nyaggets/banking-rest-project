import { URL_BASE, API_BASE, cards, showClientLogin } from '/js/utils/sharedData.js'
import { formatAmount, showToast, formatPhoneOrCard, formatCardOption, showSpinner, getData, processResponse } from '/js/utils/sharedFunctions.js'
const DESCRIPTION_MAX = 50

showSpinner()
showClientLogin()
const url = window.location.search
const search = new URLSearchParams(url)
//обработка селектов получателя и отправителя
let urlSenderCard
if (search.has('from')) {
    const senderCard = await getData(`${API_BASE}/cards/${search.get('from')}`)
    urlSenderCard = senderCard.id
}
if (!urlSenderCard) 
    urlSenderCard = cards[0]?.id

const fillSelect = (selectEl) => {
    cards.forEach(card => {
        selectEl.add(new Option(formatCardOption(card), card.id))
    })
}
const syncSelects = (initialSenderValue = null) => {
    internalSelect.querySelectorAll('option').forEach(opt => opt.disabled = false)
    senderSelect.querySelectorAll('option').forEach(opt => opt.disabled = false)
    if (initialSenderValue) 
        senderSelect.value = initialSenderValue
    const currentSenderValue = senderSelect.value
    const currentReceiverValue = internalSelect.value

    if (currentSenderValue) {
        const opt = internalSelect.querySelector(`option[value="${currentSenderValue}"]`)
        if (opt) 
            opt.disabled = true
    }
    if (!currentReceiverValue || internalSelect.querySelector(`option[value="${currentReceiverValue}"]`)?.disabled) {
        const firstAvailable = [...internalSelect.options].find(o => !o.disabled && o.value)
        if (firstAvailable) 
            internalSelect.value = firstAvailable.value
    }

    const newReceiverValue = internalSelect.value
    if (newReceiverValue) {
        const opt = senderSelect.querySelector(`option[value="${newReceiverValue}"]`)
        if (opt) 
            opt.disabled = true
    }
    const newSenderValue = senderSelect.value
    if (newSenderValue && senderSelect.querySelector(`option[value="${newSenderValue}"]`)?.disabled) {
        const firstAvailable = [...senderSelect.options].find(o => !o.disabled && o.value)
        if (firstAvailable) 
            senderSelect.value = firstAvailable.value
    }
}

const senderSelect = document.getElementById('sender-cards-select')
const internalRec = document.querySelector('.internal-rec')
const internalSelect = document.querySelector('.receiver-cards-select')
const externalInput = document.querySelector('.external-rec')
const type = search.get('type')
if (!search || type == 'INTERNAL') {
    externalInput.remove()
    internalRec.hidden = false

    fillSelect(senderSelect)
    fillSelect(internalSelect)

    syncSelects(urlSenderCard)
    internalSelect.addEventListener('change', () => syncSelects())
    senderSelect.addEventListener('change', () => syncSelects())
} 
else {
    internalRec.remove()
    externalInput.hidden = false

    fillSelect(senderSelect)
    if (urlSenderCard) 
        senderSelect.value = urlSenderCard
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
    if (!recipientNameEl)
        return

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

//ограничение длины комментария
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

//расчет и форматирование комиссии
const transferBtn = document.getElementById('transfer-btn')
const commissionEl = document.getElementById('commission-amount')
const calcCommission = async (amountInputEl) => {
    const rawValue = amountInputEl.value.replace(/[^\d.]/g, '').replace(',', '.')
    const numericValue = parseFloat(rawValue) || 0    
    amountInputEl.value = formatAmount(amountInputEl.value)
    
    if (numericValue < 100000) {
        commissionEl.innerText = ''
        transferBtn.innerText = `Перевести ${amountInputEl.value} ₽`
        return
    }
    
    const response = await fetch(`${API_BASE}/cards/transactions/transfer/commission?amount=${rawValue}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json;charset=utf-8' },
    })
    
    if (response.ok) {
        const { commission } = await response.json()
        const commissionFormatted = formatAmount(commission.toString())
        commissionEl.innerText = `Комиссия: ${commissionFormatted}₽`
        
        const totalAmount = numericValue + commission
        const totalAmountFormatted = formatAmount(totalAmount.toString())
        transferBtn.innerText = `Перевести ${totalAmountFormatted} ₽`
    } else 
        transferBtn.innerText = `Перевести ${amountInputEl.value} ₽`
}

//форматирование суммы
const amountInput = document.getElementById('amount')
amountInput.addEventListener('input', async () => await calcCommission(amountInput))

const amountBtns = document.querySelectorAll('.amount-btn')
amountBtns.forEach(btn => {
    btn.addEventListener('click', () => {
        amountInput.value = btn.innerText.replace(/[^\d\s,]/g, '')
        amountInput.dispatchEvent(new Event('input'))
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
            amount: amountInput.value.replace(/\s/g, '').replace(',', '.'),
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
    transferBtn.blur()
})