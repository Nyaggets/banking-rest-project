import { URL_BASE, client, getData } from './utils/getData.js'
import { showHistory, formatDate } from './utils/processData.js'

const url = window.location.search
const cardId = new URLSearchParams(url).get('id')

const card = await getData(`${URL_BASE}/clients/${client.id}/cards/card?id=${cardId}`)
document.getElementById('card-container').innerHTML += `<h3>${card.cardNumber}</h3>
    <h2>${card.balance}₽</h2>
    <h3>${formatDate(card.createdDate)}</h3>`

document.getElementById('transfer-btn').addEventListener('click', function() {
    window.location.assign(`${URL_BASE}/transfer?from=${cardId}`)
})
document.getElementById('replenish-btn').addEventListener('click', function() {
    window.location.assign(`${URL_BASE}/transfer?to=${cardId}`)
})

const cardHistory = document.getElementById('card-history')
const history = Array.from(await getData(`${URL_BASE}/cards/${cardId}/transactions`))
showHistory(history, cardHistory)