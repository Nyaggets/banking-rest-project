import { URL_BASE, cards, client, getData } from './utils/getData.js'
import { processResponse, formatAmount } from './utils/processData.js'

const url = window.location.search
const search = new URLSearchParams(url)
let choosedSenderCard
let choosedReceiverCard
if (search.has('from')) {
    choosedSenderCard = await getData(`${URL_BASE}/clients/${client.id}/cards/card?id=${search.get('from')}`)
}
else if (search.has('to')) {
    choosedReceiverCard = await getData(`${URL_BASE}/clients/${client.id}/cards/card?id=${search.get('to')}`)
    document.getElementById('receiver').value = choosedReceiverCard.cardNumber
    cards.slice(cards.indexOf(choosedReceiverCard), 1)
}
const senderSelect = document.getElementById('sender-cards-select')
cards.forEach((card, key) => {
    if (choosedSenderCard && card.id == choosedSenderCard.id) {
        senderSelect[key] = new Option(`${card.cardNumber} ${card.balance}`, card.id, true, true)
    }
    else {
        senderSelect[key] = new Option(`${card.cardNumber} ${card.balance}`, card.id, false, false)
    }
})

//считаю комиссию
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
    const receiverCard = document.getElementById('receiver').value
    const response = await fetch(`${URL_BASE}/cards/${senderSelect.value}/transactions/transfer`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json;charset=utf-8'
        },
        body: JSON.stringify({
            senderCardId: senderSelect.value,
            receiverCardNumber: receiverCard, 
            amount: amountInput.value.replace(/\s/g, '').replace(/\./g, ','),
            description: document.getElementById('description').value
        })
    })
    processResponse(response);
})