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
            <h5>${card.balance}</h5>
            <p>${card.createdDate}</p>
        </div>`

    cardElem.addEventListener('click', function(e) {
        e.preventDefault()
        window.location.assign(`${URL_BASE}/card?id=${card.id}`)
    })
})

const recentList = document.getElementById('recent-list')
showHistory(history, recentList, RECENT_HISTORY_LENGTH)