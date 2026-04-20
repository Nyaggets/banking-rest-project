import { URL_BASE, cards, history } from './utils/getData.js'
import { showHistory } from './utils/processData.js'
const RECENT_HISTORY_LENGTH = 5

const cardList = document.getElementById('card-list')
cards.forEach(card => {
    const cardElem = document.createElement('li')
    cardList.appendChild(cardElem)
    cardElem.innerHTML =
        `<div class="card-item">
            <h3>${card.cardNumber}</h3>
            <h2>${card.balance} ₽</h2>
            <button class="secondary-btn transfer-btn">Перевести</button>
        </div>`

    const transferBtn = cardElem.querySelector('.transfer-btn')
    transferBtn.addEventListener('click', () => {
        window.location.assign(`${URL_BASE}/transfer?from=${card.id}`)
    })

    cardElem.addEventListener('click', (e) => {
        if (e.target.tagName !== 'BUTTON')
            window.location.assign(`${URL_BASE}/card?id=${card.id}`)
    })
})

const recentList = document.getElementById('recent-list')
showHistory(history, recentList, RECENT_HISTORY_LENGTH)