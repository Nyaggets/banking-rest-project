import { URL_BASE, cards, history, showHistory, showClientLogin } from '/js/utils/sharedData.js'
import { formatAmount, showSpinner } from '/js/utils/sharedFunctions.js'
const RECENT_HISTORY_LENGTH = 5
showSpinner()

showClientLogin()
const cardList = document.getElementById('card-list')
cards.forEach(card => {
    const cardElem = document.createElement('li')
    cardList.appendChild(cardElem)
    cardElem.innerHTML =
        `<div class="card-item">
            <h3>${card.hiddenNumber}</h3>
            <h2>${formatAmount(card.balance)}₽</h2>
            <a href='${URL_BASE}/transfer?type=EXTERNAL&from=${card.id}'class="secondary-btn transfer-btn btn-link text-center" role='button'>Перевести</a>
        </div>`

    cardElem.addEventListener('click', (e) => {
        if (e.target.tagName !== 'BUTTON')
            window.location.assign(`${URL_BASE}/card?id=${card.id}`)
    })
})

const recentList = document.getElementById('recent-list')
showHistory(history, recentList, RECENT_HISTORY_LENGTH)