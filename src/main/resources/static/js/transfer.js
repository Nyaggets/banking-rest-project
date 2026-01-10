import { URL_BASE, cards, client, getData } from './utils/getData.js'

const url = window.location.search
const search = new URLSearchParams(url)
let senderCard
if (search.has('from')) {
    const cardId = new URLSearchParams(url).get('from')
    senderCard = await getData(`${URL_BASE}/clients/${client.id}/cards/card?id=${cardId}`)
}
else if (search.has('to')) {
    const cardId = new URLSearchParams(url).get('to')
    const receiverCard = await getData(`${URL_BASE}/clients/${client.id}/cards/card?id=${cardId}`)
    document.getElementById('receiver').value = receiverCard.cardNumber
    cards.slice(cards.indexOf(receiverCard), 1)
}
const senderSelect = document.getElementById('sender-cards-select')
cards.forEach((card, key) => {
    if (senderCard && card.id == senderCard.id) {
        senderSelect[key] = new Option(`${card.cardNumber} ${card.balance}`, card.id, true, true)
        console.log('inside first senderCard = ', senderCard, ' card = ', card)
    }
    else {
        senderSelect[key] = new Option(`${card.cardNumber} ${card.balance}`, card.id, false, false)
        console.log('inside third senderCard = ', senderCard, ' card = ', card)
    }
})


const transferForm = document.getElementById('transfer-form')
transferForm.addEventListener('submit', async function(e) {
    e.preventDefault()

    const senderCardId = document.getElementById('sender-cards-select').value
    const receiver = document.getElementById('receiver').value
    const receiverCard = await getData(`${URL_BASE}/clients/${client.id}/cards/search?number=${receiver}`)
    console.log(receiver, receiverCard, receiverCard.id)

    const response = await fetch(`${URL_BASE}/cards/${senderCardId}/transactions/transfer`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json;charset=utf-8'
        },
        body: JSON.stringify({
            type: 'Перевод',
            senderCardId,
            receiverCardId: receiverCard.id,
            amount: document.getElementById('amount').value,
            description: document.getElementById('description').value
        })
    })

    if (response.ok) {
        alert('Отправлено!')
    }
})