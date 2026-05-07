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
                <p class="caption">${transaction.type}  —  ${formatDate(transaction.timestamp)}</p>
                <span class="transaction-main"><h3>${transaction.counterpartyName}</h3>
                <h3 class="transaction-amount">${signIcon} ${transaction.totalAmount}₽</h3></span>
            </div>`
    }
}

const formatDate = (date) => {
    const formattedDate = new Date(date).setHours(0, 0, 0, 0)
    const yesterday = new Date(new Date().setDate(new Date().getDate() - 1))
    if (new Date().setHours(0, 0, 0, 0) == formattedDate)
        return 'Сегодня'
    else if (yesterday.setHours(0, 0, 0, 0) == formattedDate)
        return 'Вчера'
    else 
        return `${new Date(date).toLocaleDateString('ru-RU')}`
}

const processResponse = async (response) => {
    if (response.status === 401) { 
        showToast('Операция отклонена', 'Пользователь не найден')
        setTimeout(() => window.location.assign('/login'), 1600)
    }
    else if (response.status === 500) { 
        showToast('Ошибка сервера', 'Запрос не может быть выполнен. Попробуйте ещё раз позже')
    }
    else if (response.status == 400 || response.status == 404 || response.status == 403){
        const { errors: errorMap } = await response.json()
        Object.entries(errorMap).forEach(([field, message]) => {
            document.querySelector(`[error-for="${field}"]`).hidden = false
            document.querySelector(`[error-for="${field}"]`).textContent = message
        })
    }
    else if (response.status == 429) {
        const { errors: { field, message, expiresAt } } = await response.json()
        console.log(field, message, expiresAt)
        document.querySelector(`[error-for="${field}"]`).hidden = false
        document.querySelector(`[error-for="${field}"]`).textContent = `${message} Повторите ещё раз после ${new Date(expiresAt).toLocaleTimeString([], {timeStyle: 'short'})}`
    }
}

const showToast = (title, description) => {
    const toastInstance = new bootstrap.Toast(document.querySelector('.toast'))
    document.getElementById('toast-header').innerText = title
    document.getElementById('toast-body').innerText = description
    toastInstance.show();
}

const formatAmount = (amount) => {
    const cleanedString = amount.replace(/[^\d.,]/g, '').replace(/\./g, ',')
    const parts = cleanedString.split('.')
    const decimalLmited = parts[1] ?  '.' + parts[1].slice(0, 2) : '';
    const mainPart = parts[0].replace(/\B(?=(\d{3})+(?!\d))/g, ' ')
    return mainPart + decimalLmited
}

export { showHistory, formatDate, processResponse, formatAmount, showToast}