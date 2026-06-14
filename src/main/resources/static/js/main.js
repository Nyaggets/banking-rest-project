import { URL_BASE, cards, history, showHistory, showClientLogin } from '/js/utils/sharedData.js'
import { formatAmount, showSpinner, createNewElement } from '/js/utils/sharedFunctions.js'
const RECENT_HISTORY_LENGTH = 5
showSpinner()

showClientLogin()
const cardList = document.getElementById('card-list')
cards.forEach(card => {
    const cardElem = document.createElement('li')
    cardList.appendChild(cardElem)
    const cardItem = createNewElement('div', 'card-item')
    const cardNumber = createNewElement('h3', '', card.hiddenNumber)
    const balance = createNewElement('h2', '', `${formatAmount(card.balance)}₽`)
    const transferBtn = createNewElement('a', 'secondary-btn transfer-btn btn-link text-center', 'Перевести')
    transferBtn.href = `${URL_BASE}/transfer?type=EXTERNAL&from=${card.id}`
    transferBtn.role = 'button'
    cardItem.append(cardNumber, balance, transferBtn)
    cardElem.appendChild(cardItem)

    cardElem.addEventListener('click', (e) => {
        if (e.target.tagName !== 'BUTTON')
            window.location.assign(`${URL_BASE}/card?id=${card.id}`)
    })
})

const recentList = document.getElementById('recent-list')
showHistory(history, recentList, RECENT_HISTORY_LENGTH)