import { URL_BASE, cards, client, getData } from './utils/getData.js'

const url = window.location.search
const search = new URLSearchParams(url)
let senderCard
let receiverCard
if (search.has('from')) {
    senderCard = await getData(`${URL_BASE}/clients/${client.id}/cards/card?id=${search.get('from')}`)
}
else if (search.has('to')) {
    receiverCard = await getData(`${URL_BASE}/clients/${client.id}/cards/card?id=${search.get('to')}`)
    document.getElementById('receiver').value = receiverCard.cardNumber
    cards.slice(cards.indexOf(receiverCard), 1)
}
const senderSelect = document.getElementById('sender-cards-select')
cards.forEach((card, key) => {
    if (senderCard && card.id == senderCard.id) {
        senderSelect[key] = new Option(`${card.cardNumber} ${card.balance}`, card.id, true, true)
    }
    else {
        senderSelect[key] = new Option(`${card.cardNumber} ${card.balance}`, card.id, false, false)
    }
})

const transferForm = document.getElementById('transfer-form')
transferForm.addEventListener('submit', async function(e) {
    e.preventDefault()
    const senderCardId = document.getElementById('sender-cards-select').value
    receiverCard = document.getElementById('receiver').value
    const response = await fetch(`${URL_BASE}/cards/${senderCardId}/transactions/transfer`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json;charset=utf-8'
        },
        body: JSON.stringify({
            senderCardId,
            receiverCardNumber: receiverCard, 
            amount: document.getElementById('amount').value,
            description: document.getElementById('description').value
        })
    })

    if (response.ok) {
        //тост сюда бахнуть
        alert('Отправлено!')
    }
    else {
        const errorMap = await response.json()
        Object.entries(errorMap).forEach(([field, message]) => {
            document.querySelector(`[name="${field}"]`).classList.add('in-valid')
            document.querySelector(`[data-error-for="${field}"]`).hidden = false
            document.querySelector(`[data-error-for="${field}"]`).textContent = message
        })
    }
})