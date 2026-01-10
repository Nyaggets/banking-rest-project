import { history } from './getData.js';

const showHistory = function(transactions, historyList, length = transactions.length) {
    for (let i = 0; i < length && i < transactions.length ; i++) {
        const transaction = history[i];
        const transactionElem = document.createElement('li')
        historyList.appendChild(transactionElem)
        transactionElem.innerHTML = 
        `<div class="transaction-container">
            <h3>${transaction.type}\t${transaction.sign}${transaction.amount}</h3>
            <p>${transaction.counterpartyName}</p>
            <p>${transaction.timestamp}</p>
        </div>`
    }
}

export { showHistory }