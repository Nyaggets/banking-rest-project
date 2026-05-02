import { URL_BASE, client, getData } from './utils/getData.js'
import { showHistory, formatDate, processResponse } from './utils/processData.js'

const url = window.location.search
const cardId = new URLSearchParams(url).get('id')

const card = await getData(`${URL_BASE}/clients/${client.id}/cards/card?id=${cardId}`)
document.getElementById('card-container').innerHTML += `<h3 id='card-number'>${card.hiddenNumber}</h3>
    <h3 id='cvv'>***</h3>
    <h2>${card.balance}₽</h2>
    <h3>${formatDate(card.expiredDate)}</h3>
    <button type='btn' class='secondary-btn' data-bs-toggle="modal" data-bs-target="#confirm-modal">Показать</button>`

document.getElementById('transfer-btn').addEventListener('click', function() {
    window.location.assign(`${URL_BASE}/transfer?type=EXTERNAL&from=${cardId}`)
})
document.getElementById('replenish-btn').addEventListener('click', function() {
    window.location.assign(`${URL_BASE}/transfer?type=INTERNAL&to=${cardId}`)
})

if (document.getElementById('password-conf-btn')) {
    document.getElementById('password-conf-btn').addEventListener('click', async () => {
        const details = await fetch(`${URL_BASE}/card/${cardId}/card-details`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json;charset=utf-8' },
            body: JSON.stringify({ password: document.getElementById('password-input').value })
        })
        if (details.ok) {
            const { cardNumber, cvv } = await details.json()
            document.getElementById('card-number').innerText = cardNumber
            document.getElementById('cvv').innerText = cvv

            var genericModalEl = document.getElementById('confirm-modal')
            var modal = bootstrap.Modal.getInstance(genericModalEl)
            modal.hide()

        }
        else if (details.status == '401') {
            const errorEl = document.getElementById('confirm-error')
            errorEl.hidden = false
            errorEl.innerText = 'Неверный пароль'
        }
        else if (details.status == '429') {
            document.getElementById('password-input').readOnly = true
            const errorEl = document.getElementById('confirm-error')
            errorEl.hidden = false
            errorEl.innerText = 'Лимит попыток исчерпан'
        }
    })
}

const cardHistory = document.getElementById('card-history')
const history = Array.from(await getData(`${URL_BASE}/cards/${cardId}/transactions`))
showHistory(history, cardHistory)