import { history } from './getData.js'

const showHistory = (transactions, historyList, length = transactions.length) => {
    for (let i = 0; i < length && i < transactions.length ; i++) {
        const transaction = history[i];
        const transactionElem = document.createElement('li')
        historyList.appendChild(transactionElem)
        let signIcon
        switch (transaction.direction) {
            case 'in':
                signIcon = '<i class="fa fa-plus" aria-hidden="true"></i>'
                break
            case 'out':
                signIcon = '<i class="fa fa-minus" aria-hidden="true"></i>'
                break
            case 'between':
                signIcon = '<i class="fa fa-refresh" aria-hidden="true"></i>'
                break
        }
        transactionElem.innerHTML = `<div class="transaction-item transaction-${transaction.direction}">
            <p class="caption">${transaction.type} ${formatDate(transaction.timestamp)}</p>
            <span class="transaction-main"><h3>${transaction.counterpartyName}</h3>
            <h3 class="transaction-amount">${signIcon} ${transaction.amount}₽</h3></span>
        </div>`
    }
}

const formatDate = (date) => {
    const formattedDate = new Date(date).setHours(0, 0, 0, 0)
    const yesterday = new Date(new Date().setDate(new Date().getDate() - 1));
    if (new Date().setHours(0, 0, 0, 0) == formattedDate)
        return 'Сегодня'
    else if (yesterday.setHours(0, 0, 0, 0) == formattedDate)
        return 'Вчера'
    else 
        return new Date(date).toLocaleDateString('ru-RU')
}

export { showHistory, formatDate }