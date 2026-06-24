import { formatDate, formatAmount, formatPhoneOrCard, createNewElement, getData } from './sharedFunctions.js'
const URL_BASE = 'http://localhost:8080'
const API_BASE = 'http://localhost:8080/api'

const showClientLogin = () => document.getElementById('profile-login').innerText = client.login
const client = await getData(`${API_BASE}/clients/me`)
const cards = Array.from(await getData(`${API_BASE}/cards`))
const { content: history, totalPages } = await getData(`${API_BASE}/cards/history`)

const typesRu = {
  DEPOSIT: 'Зачисление',
  TRANSFER_IN: 'Входящий перевод',
  WITHDRAWAL: 'Списание',
  TRANSFER_OUT: 'Перевод',
  TRANSFER_BETWEEN: 'Перевод между своими',
  DEFAULT: 'Тип операции не определен'
}

const operationIcons = {
  CLIENT: 'money-bill-transfer',
  MERCHANT: 'receipt',
  PURCHASE: 'shopping-basket',
  SALARY: 'building',
  REFUND: 'undo',
  DEFAULT: 'times'
}

const amountIcons = {
  IN: 'plus',
  OUT: 'minus',
  BETWEEN: 'refresh',
  DEFAULT: 'times'
}

const refactorTransaction = (transaction) => {
  if (!transaction)
    return

  transaction.signIcon = amountIcons[transaction.direction.toUpperCase()] || amountIcons.DEFAULT
  transaction.operationIcon = operationIcons[transaction.counterpartyType.toUpperCase()] || operationIcons.DEFAULT
  transaction.operationTypeRu = typesRu[transaction.operationType.toUpperCase()] || typesRu.DEFAULT
  if (transaction.counterpartyType == 'PURCHASE') 
    transaction.operationTypeRu = 'Оплата товаров или услуг'
  else if (transaction.counterpartyType == 'SALARY') 
    transaction.operationTypeRu = 'Зарплата'
  else if (transaction.counterpartyType == 'REFUND') 
    transaction.operationTypeRu = 'Возврат'
  
  if (transaction.counterpartyIdentifier?.length == 4)
    transaction.counterpartyIdentifier = `****${transaction.counterpartyIdentifier}`
  else if (transaction.counterpartyIdentifier?.length == 11) {
    const identifier = formatPhoneOrCard(transaction.counterpartyIdentifier) ?? 'Не найдено'
    transaction.counterpartyName = `${transaction.counterpartyName} (${identifier})`
  }
}

const showHistory = (transactions, historyList, length = transactions.length) => {
  historyList.innerHTML = ''
  transactions.forEach(transaction => {
    refactorTransaction(transaction)
    const transactionElem = document.createElement('li')
    historyList.appendChild(transactionElem)
    const signIcon = `<i class="fa fa-${transaction.signIcon}" aria-hidden="true"></i>`   
    const operationIcon =  `<i class="fa fa-${transaction.operationIcon} operaton-icon" aria-hidden="true"></i>`   
    
    const amount = createNewElement('h3', 'transaction-amount flex-shrink-0 text-nowrap ms-2', `${signIcon} ${formatAmount(transaction.totalAmount)}₽`)
    const counterparty = createNewElement('h3', 'flex-grow-1', `${operationIcon} ${transaction.counterpartyName}`)
    const textContainer = createNewElement('span', 'transaction-main d-flex align-items-center')
    const operationTimestamp = createNewElement('p', 'caption', `${transaction.operationTypeRu} — ${formatDate(transaction.timestamp)}`)
    const transactionDiv = createNewElement('div', `transaction direction-${transaction.direction}`)
    transactionDiv.dataset.id = transaction.id

    textContainer.appendChild(counterparty)
    textContainer.appendChild(amount)
    transactionDiv.appendChild(operationTimestamp)
    transactionDiv.appendChild(textContainer)
    transactionElem.appendChild(transactionDiv)
  })

  const transactionElems = document.querySelectorAll('.transaction')
  transactionElems.forEach(elem => {
    elem.addEventListener('click', (e) => {
      const parentDiv = e.target.closest('.transaction')
      window.location.assign(`${URL_BASE}/transaction?operationId=${parentDiv.dataset.id}`)
    })
  })
}

export { URL_BASE, API_BASE, client, cards, history, totalPages, showClientLogin, refactorTransaction, showHistory }