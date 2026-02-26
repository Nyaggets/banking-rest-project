import { URL_BASE, sendRequest } from './utils/getData.js'

document.getElementById('register-button').addEventListener('click', function(e) {
    e.preventDefault()
    window.location.assign(`${URL_BASE}/sigin`)
}) 

const clientsList = document.getElementById('clients-list')
const cardsList = document.getElementById('cards-list')
cardsList.style.display = 'none'
const transactionsList = document.getElementById('transactions-list')
transactionsList.style.display = 'none'

document.getElementById('clients-button').addEventListener('click', function(e) {
    e.preventDefault()
    clientsList.style.display = 'block'
    cardsList.style.display = 'none'
    transactionsList.style.display = 'none'
})
document.getElementById('cards-button').addEventListener('click', function(e) {
    e.preventDefault()
    clientsList.style.display = 'none'
    cardsList.style.display = 'block'
    transactionsList.style.display = 'none'
})
document.getElementById('transactions-button').addEventListener('click', function(e) {
    e.preventDefault()
    clientsList.style.display = 'none'
    cardsList.style.display = 'none'
    transactionsList.style.display = 'block'
})

const clients = Array.from(await sendRequest(`${URL_BASE}/clients`))
clients.forEach(client => {
    const clientElem = document.createElement('tr')
    clientsList.appendChild(clientElem)
    clientElem.innerHTML = `<td>${client.id}</td>
        <td>${client.name}</td>
        <td>${client.phone}</td>
        <td>${client.password}</td>` 
})

const cards = Array.from(await sendRequest(`${URL_BASE}/cards`))
cards.forEach(card => {
    const cardElem = document.createElement('tr')
    cardsList.appendChild(cardElem)
    cardElem.innerHTML = `<td>${card.id}</td>
        <td>${card.cardNumber}</td>
        <td>${card.balance}</td>
        <td>${card.clientId}</td>
        <td>${card.createdDate}></td>`
})

const transactions = Array.from(await sendRequest(`${URL_BASE}/transactions`))
transactions.forEach(transaction => {
    const transactionElem = document.createElement('tr')
    transactionsList.appendChild(transactionElem)

    transactionElem.innerHTML = `<td>${transaction.timestamp}</td>
        <td>${transaction.type}</td>
        <td>${transaction.amount}</td>`
    switch (transaction.type) {
        case 'Оплата товаров и услуг':
            transactionElem.innerHTML += 
                `<td>${transaction.merchant}</td>
                <td>${transaction.senderCardNumber}; ${transaction.senderDetails.name}</td>`
            break

        case 'Зачисление':
            transactionElem.innerHTML += 
                `<td>${transaction.receiverCardNumber}; ${transaction.receiverDetails.name}</td>
                <td>${transaction.source}</td>`
            break
            
        case 'Перевод':
            transactionElem.innerHTML += 
                `<td>${transaction.receiverCardNumber}; ${transaction.receiverDetails.name}</td>
                <td>${transaction.senderCardNumber}; ${transaction.senderDetails.name}</td>`
            break
    }
})