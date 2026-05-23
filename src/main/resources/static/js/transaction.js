import { URL_BASE } from "./utils/getData.js"
import { renderTransaction, formatAmount, formatDate } from "./utils/processData.js"

const operationId = new URLSearchParams(window.location.search).get('operationId')
const response = await fetch(`${URL_BASE}/cards/transactions?operationId=${operationId}`)
let transaction
if (response.ok) {
    transaction = await response.json()
    renderTransaction(transaction)
}

const createInfoBlock = (icon, labelText, valueText, container) => {
  const label = document.createElement('p')
  label.classList.add('detail-label', 'caption', 'mb-1')
  label.innerHTML = `<i class="${icon} me-2"></i>${labelText}`
  const title = document.createElement('h3')
  title.classList.add('mb-0')
  title.innerText = valueText
  
  container.appendChild(label)
  container.appendChild(title)
}

document.getElementById('operation-title').innerHTML = transaction.typeRu
document.getElementById('amount').innerText = `${formatAmount(transaction.amount.toString())} ₽`
const commissionEl = document.getElementById('commission')
const commisionText = transaction.commission != 0 ? `${formatAmount(transaction.commission.toString())} ₽` : 'Нет комисии'
commissionEl.innerText = commisionText
if (transaction.commission == 0)
    commissionEl.classList.add('grey-text')

const cardsContainer = document.getElementById('counterparty-container')
console.log(transaction)
cardsContainer.innerHTML = '' 
if (transaction.direction === 'out') {
  createInfoBlock('fa fa-credit-card text-muted', 'Карта списания', transaction.senderCardNumber, cardsContainer)
} 
else if (transaction.direction === 'in') {
  createInfoBlock('fa fa-credit-card text-muted', 'Карта зачисления', transaction.receiverIdentifier, cardsContainer)
} 
else if (transaction.type == 'TRANSFER') { 
  createInfoBlock('fa fa-credit-card text-muted', 'Карта списания', transaction.senderCardNumber, cardsContainer)
  const secondCard = document.createElement('div')
  secondCard.classList.add('mt-4')
  cardsContainer.appendChild(secondCard)
  createInfoBlock('fa fa-credit-card text-muted', 'Карта зачисления', transaction.receiverIdentifier, secondCard)
}

document.getElementById('date').innerText = 
        new Intl.DateTimeFormat('ru', {dateStyle: 'long', timeStyle: 'short'}).format(new Date(transaction.timestamp))

const msgContainer = document.getElementById('message-container')
msgContainer.innerHTML = ''
if (transaction.description) 
  createInfoBlock('fa-solid fa-message', 'Комментарий', `“${transaction.description}”`, msgContainer)
else
    msgContainer.remove()