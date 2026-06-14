import { URL_BASE, API_BASE, showHistory, showClientLogin } from '/js/utils/sharedData.js'
import { formatAmount, showSpinner, createNewElement, getData, processResponse } from '/js/utils/sharedFunctions.js'

showSpinner()
showClientLogin()
const url = new URLSearchParams(window.location.search)
const cardId = url.get('id')
const card = await getData(`${API_BASE}/cards/${cardId}`)

const cardContainer = document.getElementById('card-container')
const cardNumberEl = createNewElement('h3', '', card.hiddenNumber)
cardNumberEl.id = 'card-number'
const cvvEl = createNewElement('h3', '', '***')
cvvEl.id = 'cvv'
const balanceEl = createNewElement('h2', '', `${formatAmount(card.balance)}₽`)
const expiredDateEl = createNewElement('h3', '', new Date(card.expiredDate).toLocaleDateString())
const showBtn = createNewElement('button', 'secondary-btn', 'Показать')
showBtn.type = 'button'
showBtn.dataset.bsToggle = 'modal'
showBtn.dataset.bsTarget = '#confirm-modal'

cardContainer.append(cardNumberEl, cvvEl, balanceEl, expiredDateEl, showBtn)
document.querySelectorAll('.transfer-btn').forEach(btn => {
    btn.href = `${URL_BASE}/transfer?type=EXTERNAL&from=${cardId}`
})
document.getElementById('withdrawal-btn').href = `${URL_BASE}/balance-top-up?from=${cardId}`

const stats = await getData(`${API_BASE}/cards/${cardId}/stats`)
document.getElementById('month-income').innerHTML = `<i class="fa fa-plus" aria-hidden="true"></i> ${formatAmount(stats.income)}₽`
document.getElementById('month-outcome').innerHTML = `<i class="fa fa-minus" aria-hidden="true"></i> ${formatAmount(stats.outcome)}₽`
document.getElementById('month-name').innerText  = `Статистика за ${new Date().toLocaleString('ru', {month: 'long'})}`

document.getElementById('account-number').innerText = card.accountNumber || 'Номер не определен'

const cardHistoryLink = `${URL_BASE}/history?cardId=${cardId}`
document.getElementById('card-history-link').href = cardHistoryLink
document.getElementById('card-history-btn').addEventListener('click', () => {
    window.location.assign(cardHistoryLink)
})

const cardHistoryPage = await getData(`${API_BASE}/cards/history?cardId=${cardId}`)
const { content: cardHistory } = cardHistoryPage
if (!cardHistory || cardHistory.length == 0) {
    const errorMsgEl = document.getElementById('history-msg')
    errorMsgEl.hidden = false
    const title = createNewElement('h3', '', 'История операций пуста')
    const subtitle = createNewElement('p', 'caption', 'Совершите новый перевод сейчас')
    const btn = createNewElement('a', 'main-btn btn-link mt-4', 'Новый перевод')
    btn.role = 'button'
    btn.id = 'clear-filters-btn'
    btn.href = `${URL_BASE}/transfer?type=EXTERNAL&from=${cardId}`

    errorMsgEl.appendChild(title)
    errorMsgEl.appendChild(subtitle)
    errorMsgEl.appendChild(btn)
}
else {
    const cardTransactionsList = document.getElementById('card-history')
    showHistory(cardHistory, cardTransactionsList)
}


const modalEl = document.getElementById('confirm-modal')
const modalInput = document.getElementById('password-input')
const modalError = document.getElementById('confirm-error')
modalEl.addEventListener('hidden.bs.modal', () => {
    modalError.value = ''
})
document.getElementById('password-conf-form')?.addEventListener('submit', async (e) => {
    e.preventDefault()
    const errorEl = document.getElementById('confirm-error')
    const response = await fetch(`${API_BASE}/cards/${cardId}/card-details`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json;charset=utf-8' },
        body: JSON.stringify({ password: modalInput.value })
    })
    if (response.ok) {
        const { cardNumber, cvv } = await response.json()
        document.getElementById('card-number').innerText = cardNumber
        document.getElementById('cvv').innerText = cvv
        bootstrap.Modal.getInstance(modalEl).hide()
    }
    else
        processResponse(response)
})