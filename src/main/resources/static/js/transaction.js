import { URL_BASE, renderTransaction, formatAmount, formatDate, showClientLogin } from "./utils/sharedData.js"

showClientLogin()
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
  label.innerHTML = `<i class="fa ${icon} me-2"></i>${labelText}`
  const title = document.createElement('h3')
  title.classList.add('mb-0')
  title.innerText = valueText
  
  container.appendChild(label)
  container.appendChild(title)
}

document.getElementById('operation-title').innerHTML = transaction.typeRu
document.getElementById('amount').innerText = `${formatAmount(transaction.amount)} ₽`
const commissionEl = document.getElementById('commission')
const commisionText = transaction.commission && transaction.commission != 0 ? `${formatAmount(transaction.commission)} ₽` : 'Нет комисии'
createInfoBlock('fa-solid fa-percent', 'Комиссия', commisionText, commissionEl)
if (transaction.commission == 0)
    commissionEl.classList.add('grey-text')

const counterpartyEl = document.getElementById('counterparty-container')
const clientEl = document.getElementById('client-container')
counterpartyEl.innerHTML = '' 
const formatCounterpartyInfo = () => {
  const name = transaction.counterPartyName ? ` (${transaction.counterPartyName})` : ''
  return `${transaction.counterPartyHiddenNumber}${name}`
}
commissionEl.hidden = (transaction.type !== 'WITHDRAWAL')
const isOut = transaction.type === 'TRANSFER_OUT'
if (transaction.type === 'WITHDRAWAL') {
  createInfoBlock('fa-arrow-down', 'Получатель', transaction.counterPartyName, counterpartyEl)
  createInfoBlock('fa-credit-card', 'Карта списания', transaction.clientHiddenNumber, clientEl)
} 
else if (transaction.type === 'DEPOSIT') {
  createInfoBlock('fa-arrow-up', 'Отправитель', transaction.counterPartyName, counterpartyEl)
  createInfoBlock('fa-credit-card', 'Карта зачисления', transaction.clientHiddenNumber, clientEl)
} 
else if (transaction.type.includes('TRANSFER') && transaction.direction != 'between') {
  createInfoBlock('fa-credit-card', 'Карта списания', isOut ? transaction.clientHiddenNumber : formatCounterpartyInfo(), counterpartyEl)
  createInfoBlock('fa-credit-card', 'Карта зачисления', isOut ? formatCounterpartyInfo() : transaction.clientHiddenNumber, clientEl)
}
else {
  createInfoBlock('fa-credit-card', 'Карта списания', isOut ? transaction.clientHiddenNumber : transaction.counterPartyHiddenNumber, counterpartyEl)
  createInfoBlock('fa-credit-card', 'Карта зачисления', isOut ? transaction.counterPartyHiddenNumber : transaction.clientHiddenNumber, clientEl)
}

document.getElementById('date').innerText = 
        new Intl.DateTimeFormat('ru', {dateStyle: 'long', timeStyle: 'short'}).format(new Date(transaction.timestamp))

const msgContainer = document.getElementById('message-container')
msgContainer.innerHTML = ''
if (transaction.description) 
  createInfoBlock('fa-solid fa-message', 'Комментарий', `“${transaction.description}”`, msgContainer)
else
    msgContainer.remove()