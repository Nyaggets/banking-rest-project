import { URL_BASE, client, getData, history, totalPages } from './utils/getData.js'
import { showHistory, formatDate, formatAmount, processResponse } from './utils/processData.js'

const url = new URLSearchParams(window.location.search)
const cardId = url.get('id')

const card = await getData(`${URL_BASE}/clients/${client.id}/cards?id=${cardId}`)
document.getElementById('card-container').innerHTML += `<h3 id='card-number'>${card.hiddenNumber}</h3>
    <h3 id='cvv'>***</h3>
    <h2>${formatAmount(card.balance.toString())}₽</h2>
    <h3>${formatDate(card.expiredDate)}</h3>
    <button type='btn' class='secondary-btn' data-bs-toggle="modal" data-bs-target="#confirm-modal">Показать</button>`
document.getElementById('transfer-btn').addEventListener('click', function() {
    window.location.assign(`${URL_BASE}/transfer?type=EXTERNAL&from=${cardId}`)
})
document.getElementById('replenish-btn').addEventListener('click', function() {
    window.location.assign(`${URL_BASE}/transfer?type=INTERNAL&to=${cardId}`)
})

const stats = await getData(`${URL_BASE}/clients/${client.id}/cards/${cardId}/stats`)
document.getElementById('month-income').innerHTML = `<i class="fa fa-plus" aria-hidden="true"></i> ${formatAmount(stats.income.toString())}₽`
document.getElementById('month-outcome').innerHTML = `<i class="fa fa-minus" aria-hidden="true"></i> ${formatAmount(stats.outcome.toString())}₽`
document.getElementById('month-name').innerText  = `Статистика за ${new Date().toLocaleString('ru', {month: 'long'})}`

const modalEl = document.getElementById('confirm-modal')
const modalInput = document.getElementById('password-input')
modalEl.addEventListener('hidden.bs.modal', function (event) {
    modalInput.value = ''
})
if (document.getElementById('password-conf-btn')) {
    document.getElementById('password-conf-btn').addEventListener('click', async () => {
        const errorEl = document.getElementById('confirm-error')
        const response = await fetch(`${URL_BASE}/card/${cardId}/card-details`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json;charset=utf-8' },
            body: JSON.stringify({ password: modalInput.value })
        })
        processResponse(response);
        if (response.ok) {
            const { cardNumber, cvv } = await response.json()
            document.getElementById('card-number').innerText = cardNumber
            document.getElementById('cvv').innerText = cvv
            bootstrap.Modal.getInstance(modalEl).hide()
        }
        else if (response.status == '429') {
            modalInput.disabled = true
        }
    })
}

document.getElementById('card-history-link').href = `${URL_BASE}/history?cardId=${cardId}`

const cardHistory = document.getElementById('card-history')
showHistory(history, cardHistory)