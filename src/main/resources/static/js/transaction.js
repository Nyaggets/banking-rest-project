import { URL_BASE, API_BASE, renderTransaction, showClientLogin } from "./utils/sharedData.js"
import { formatAmount, formatDate, showSpinner } from "./utils/sharedFunctions.js"

showSpinner()
showClientLogin()
const operationId = new URLSearchParams(window.location.search).get('operationId')
const response = await fetch(`${API_BASE}/cards/transactions?operationId=${operationId}`)
let transaction
if (response.ok) {
    transaction = await response.json()
    renderTransaction(transaction)
}

const fillOperationInfo = (icon, labelText, titleText, container, titleClass) => {
  if (!container) 
    return
  const label = document.createElement('p')
  label.className = titleClass ?? ''
  label.innerHTML = `<i class='fa ${icon} me-2 mb-1 detail-label caption ${titleClass ?? ''}'></i>${labelText}`
  const title = document.createElement('h3')
  title.className = 'mb-0'
  title.innerText = titleText
  
  container.appendChild(label)
  container.appendChild(title)
}

document.getElementById('operation-title-container').classList.add(`direction-${transaction.direction}`)
document.getElementById('operation-title').innerHTML = transaction.typeRu
document.getElementById('amount').innerText = `${formatAmount(transaction.amount)} ₽`
const commissionEl = document.getElementById('commission')
const commisionText = transaction.commission && transaction.commission != 0 ? `${formatAmount(transaction.commission)} ₽` : 'Нет комисии'
fillOperationInfo('fa-solid fa-percent', 'Комиссия', commisionText, commissionEl)
if (transaction.commission == 0)
    commissionEl.classList.add('grey-text')

const counterpartyEl = document.getElementById('counterparty-container')
const clientEl = document.getElementById('client-container')
counterpartyEl.innerHTML = '' 
const formatCounterpartyText = () => {
  const name = transaction.counterPartyName ? ` (${transaction.counterPartyName})` : ''
  return `${transaction.counterPartyHiddenNumber}${name}`
}
commissionEl.hidden = (transaction.type !== 'WITHDRAWAL')
const isOut = transaction.type === 'TRANSFER_OUT'
if (transaction.type === 'WITHDRAWAL') {
  fillOperationInfo('fa-arrow-down', 'Получатель', transaction.counterPartyName, counterpartyEl, 'transaction-receiver')
  fillOperationInfo('fa-credit-card', 'Карта списания', transaction.clientHiddenNumber, clientEl, 'transaction-sender')
} 
else if (transaction.type === 'DEPOSIT') {
  fillOperationInfo('fa-arrow-up', 'Отправитель', transaction.counterPartyName, counterpartyEl, 'transaction-sender')
  fillOperationInfo('fa-credit-card', 'Карта зачисления', transaction.clientHiddenNumber, clientEl, 'transaction-receiver')
} 
else if (transaction.type.includes('TRANSFER') && transaction.direction != 'between') {
  fillOperationInfo('fa-credit-card', 'Карта списания', isOut ? transaction.clientHiddenNumber : formatCounterpartyText(), counterpartyEl, 'transaction-sender')
  fillOperationInfo('fa-credit-card', 'Карта зачисления', isOut ? formatCounterpartyText() : transaction.clientHiddenNumber, clientEl, 'transaction-receiver')
}
else {
  fillOperationInfo('fa-credit-card', 'Карта списания', isOut ? transaction.clientHiddenNumber : transaction.counterPartyHiddenNumber, counterpartyEl)
  fillOperationInfo('fa-credit-card', 'Карта зачисления', isOut ? transaction.counterPartyHiddenNumber : transaction.clientHiddenNumber, clientEl)
}

document.getElementById('date').innerText = new Intl.DateTimeFormat('ru', {dateStyle: 'long', timeStyle: 'short'}).format(new Date(transaction.timestamp))

const msgContainer = document.getElementById('message-container')
msgContainer.innerHTML = ''
if (transaction.description) 
  fillOperationInfo('fa-solid fa-message', 'Комментарий', `“${transaction.description}”`, msgContainer)
else
    msgContainer.remove()