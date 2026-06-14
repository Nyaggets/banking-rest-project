import { API_BASE, URL_BASE, cards, client, showClientLogin } from '/js/utils/sharedData.js'
import { formatCardOption, formatPhoneOrCard, formatAmount, showSpinner, showToast, getData, processResponse } from '/js/utils/sharedFunctions.js'

showSpinner()
showClientLogin()
const senderSelect = document.getElementById('sender-cards-select')
const url = window.location.search
const search = new URLSearchParams(url)
let urlSenderCard
if (search.has('from')) 
    urlSenderCard = await getData(`${API_BASE}/cards/${search.get('from')}`)
cards.forEach((card) => {
    senderSelect.add(new Option(formatCardOption(card), card.id, false, (urlSenderCard && urlSenderCard.id == card.id)))
})

const phoneInput = document.getElementById('phone')
phoneInput.addEventListener('input', () => { 
    phoneInput.value = formatPhoneOrCard(phoneInput.value) 
})
document.getElementById('my-phone-btn').addEventListener('click', () => {
    phoneInput.value = formatPhoneOrCard(client?.phone)
})

//форматирование суммы
const transferBtn = document.getElementById('withdrawal-btn')
const amountInput = document.getElementById('amount')
amountInput.addEventListener('input', async () => {
    const amountFormatted = formatAmount(amountInput.value)
    amountInput.value = amountFormatted
    transferBtn.innerText = `Перевести ${amountFormatted} ${amountFormatted ? '₽' : ''}`
})

document.getElementById('phone-form').addEventListener('submit', async (e) => {
    e.preventDefault()
    const errors = document.querySelectorAll('.error-msg')
    errors.forEach(elem => {
        elem.hidden = true
        elem.textContent = ''
    })
    const response = await fetch(`${API_BASE}/cards/${senderSelect.value}/transactions/balance-top-up`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json;charset=utf-8'
        },
        body: JSON.stringify({
            clientCardId: senderSelect.value,
            counterpartyIdentifier: phoneInput.value.replace(/\D/g, ''), 
            amount: amountInput.value.replace(/\s/g, '').replace(/\./g, ',')
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