import { URL_BASE, cards, client, getData } from './utils/getData.js'
import { processResponse, formatAmount } from './utils/processData.js'

const url = window.location.search
const search = new URLSearchParams(url)
let urlSenderCard
let urlReceiverCard
if (search.has('from')) {
    urlSenderCard = await getData(`${URL_BASE}/clients/${client.id}/cards/card?id=${search.get('from')}`)
}
else if (search.has('to')) {
    urlReceiverCard = await getData(`${URL_BASE}/clients/${client.id}/cards/card?id=${search.get('to')}`)
    document.getElementById('receiver').value = urlReceiverCard.hiddenNumber
}
const senderSelect = document.getElementById('sender-cards-select')
cards.forEach((card, key) => {
    senderSelect[key] = new Option(`${card.hiddenNumber} ${card.balance}₽`, card.id, false, (urlSenderCard && card.id == urlSenderCard.id))
})
if (search.has('type')) {
    const type = search.get('type')
    if (type == 'INTERNAL') {
        document.querySelector('.external-rec').remove()
        const internalSelect = document.querySelector('.internal-rec')
        cards.forEach((card, key) => {
            internalSelect[key] = new Option(`${card.hiddenNumber} ${card.balance}₽`, card.id, false, (urlReceiverCard && card.id == urlReceiverCard.id))
        })

        internalSelect.addEventListener('change', () => {
            if (internalSelect.value == senderSelect.value)
                senderSelect.selectedIndex = -1
        })
        senderSelect.addEventListener('change', () => {
            if (internalSelect.value == senderSelect.value)
                internalSelect.selectedIndex = -1
        })
    }
    else if (type == 'EXTERNAL')
            document.querySelector('.internal-rec').remove()
}

//расчет комиссии
const calcCommission = async (amountInputEl) => {
    const cleanedAmount = amountInputEl.value.replace(' ', '').replace('.', ',')
    const commissionEl = document.getElementById('commission-amount')
    const response = await fetch(`${URL_BASE}/cards/${senderSelect.value}/transactions/commission?amount=${cleanedAmount}`, {
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
        const totalAmountFormatted = formatAmount(totalAmount.toString())
        document.getElementById('transfer-btn').innerText = `Перевести ${totalAmountFormatted} ${totalAmountFormatted ? '₽' : ''}`
    }
}
const amountInput = document.getElementById('amount')
amountInput.addEventListener('input', async () => {
    calcCommission(amountInput)
})
const amountBtns = document.querySelectorAll('.amount-btn')
amountBtns.forEach(btn => {
    btn.addEventListener('click', () => {
        amountInput.value = btn.innerText.replace(/[^\d\s\,]/g, '')
        calcCommission(amountInput)
    })
})

const transferForm = document.getElementById('transfer-form')
transferForm.addEventListener('submit', async (e) => {
    e.preventDefault()
    const receiverIdentifier = document.getElementById('receiver').value.replace(/[\s\*]/g, '')
    const response = await fetch(`${URL_BASE}/cards/${senderSelect.value}/transactions/transfer`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json;charset=utf-8'
        },
        body: JSON.stringify({
            senderCardId: senderSelect.value,
            receiverIdentifier, 
            amount: amountInput.value.replace(/\s/g, '').replace(/\./g, ','),
            description: document.getElementById('description').value
        })
    })
    processResponse(response);
})