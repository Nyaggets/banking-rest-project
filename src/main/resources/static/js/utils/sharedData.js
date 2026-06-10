import { showToast, formatDate, formatAmount } from './sharedFunctions.js'

const URL_BASE = 'http://localhost:8080'
const API_BASE = 'http://localhost:8080/api'

const processResponse = async (response) => {
    const errorData = await response.json()
    if (response.url.includes('/login')) {
        showToast('Сессия завершена', 'Время сессии истекло, повторите вход')
        setTimeout(() => window.location.assign('/login'), 1500)
        return
    }
    const { code, errors = {}, expiresAt } = errorData 
    switch (response.status) {
        case 500:
            showToast('Ошибка сервера', 'Запрос не может быть выполнен. Попробуйте позже')
            break

        case 401:
            sessionStorage.clear()
            showToast('Сессия завершена', 'Время сессии истекло, повторите вход')
            setTimeout(() => window.location.assign('/login'), 1500)
            break

        case 429:
            const field = Object.keys(errors)[0] 
            const message = Object.values(errors)[0] || 'Лимит попыток исчерпан.'
            const errorElement = document.querySelector(`[error-for="${field}"]`)
            if (errorElement && expiresAt) {
                errorElement.hidden = false
                const expireTime = new Date(expiresAt).toLocaleTimeString([], { timeStyle: 'short' })
                errorElement.innerText = `${message} Повторите ещё раз после ${expireTime}`
            } else 
                showToast('Ошибка сервера', 'Лимит попыток превышен. Попробуйте позже')
            break

        case 400:
        case 404:
        case 403:
            document.querySelectorAll('.error-msg').forEach(elem => {
                elem.hidden = true
                elem.innerText = ''
            })
            Object.entries(errors).forEach(([field, message]) => {
                const errorElement = document.querySelector(`[error-for="${field}"]`)
                if (errorElement) {
                    errorElement.hidden = false
                    errorElement.innerText = message
                } else 
                    showToast('Ошибка', message)
            })
            break
    }
}

const getData = async (url) => {
    const response = await fetch(url)
    if (response.ok) {
        return response.json()
    }
    else
        processResponse(response)
}

const showClientLogin = () => { document.getElementById('profile-login').innerText = client.login }
const client = await getData(`${API_BASE}/clients/me`)
const cards = Array.from(await getData(`${API_BASE}/cards`))
const { content: history, totalPages} = await getData(`${API_BASE}/cards/history`)

const renderTransaction = (transaction) => {
    switch (transaction.direction) {
        case 'in':
            if (transaction.type == 'DEPOSIT')
                transaction.typeRu = 'Зачисление'
            else if (transaction.type == 'TRANSFER_IN')
                transaction.typeRu = 'Входящий перевод'
            transaction.signIcon = 'plus'
            break
        case 'out':
            if (transaction.type == 'WITHDRAWAL') 
                transaction.typeRu = 'Списание'
            else if (transaction.type == 'TRANSFER_OUT')
                transaction.typeRu = 'Перевод'
            transaction.signIcon = 'minus'
            break
        case 'between':
            transaction.typeRu = 'Перевод между своими'
            transaction.counterpartyName = client.name
            transaction.signIcon = 'refresh'
            break
    }
}

const showHistory = (transactions, historyList, length = transactions.length) => {
    historyList.innerHTML = ''
    console.log(transactions)
    transactions.forEach(transaction => {
        renderTransaction(transaction)
        const transactionElem = document.createElement('li')
        historyList.appendChild(transactionElem)
        const signIcon = `<i class="fa fa-${transaction.signIcon}" aria-hidden="true"></i>` 
        transactionElem.innerHTML = `<div class="transaction direction-${transaction.direction}" data-id=${transaction.id}> 
            <p class="caption">${transaction.typeRu} — ${formatDate(transaction.timestamp)}</p> 
            <span class="transaction-main d-flex align-items-center"> 
                <h3 class="flex-grow-1">${transaction.counterPartyName}</h3> 
                <h3 class="transaction-amount flex-shrink-0 text-nowrap ms-2">${signIcon} ${formatAmount(transaction.totalAmount)}₽</h3> 
            </span> 
        </div>` 
    })
    const transactionElems = document.querySelectorAll('.transaction')
    transactionElems.forEach(elem => {
        elem.addEventListener('click', (e) => {
            const parentDiv = e.target.closest('.transaction')
            window.location.assign(`${URL_BASE}/transaction?operationId=${parentDiv.dataset.id}`)
        })
    })
}

export { URL_BASE, API_BASE, getData, client, cards, history, totalPages, processResponse, showClientLogin, renderTransaction, showHistory }