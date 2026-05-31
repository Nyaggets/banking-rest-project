const URL_BASE = 'http://localhost:8080';

const getData = async(url) => {
    const response = await fetch(url);
    if (response.ok) {
        return response.json()
    }
    else {
        const errorMap = await response.json()
        Object.entries(errorMap).forEach(([field, message]) => {
            document.querySelector(`[name="${field}"]`).classList.add('error')
            document.querySelector(`[data-error-for="${field}"]`).textContent = message
        })
    }
}

const client = await getData(`${URL_BASE}/clients/me`)
const cards = Array.from(await getData(`${URL_BASE}/clients/${client.id}/cards`))
const { content: history, totalPages} = await getData(`${URL_BASE}/api/history`)

const showClientLogin = () => { document.getElementById('profile-login').innerText = client.login }

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
    transactions.forEach(transaction => {
        renderTransaction(transaction)
        const transactionElem = document.createElement('li')
        historyList.appendChild(transactionElem)
        const signIcon = `<i class="fa fa-${transaction.signIcon}" aria-hidden="true"></i>`
        transactionElem.innerHTML = `<div class="transaction direction-${transaction.direction}" data-id=${transaction.id}>
                <p class="caption">${transaction.typeRu}  —  ${formatDate(transaction.timestamp)}</p>
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

const formatDate = (date) => {
    const formattedDate = new Date(date).setHours(0, 0, 0, 0)
    const yesterday = new Date(new Date().setDate(new Date().getDate() - 1))
    if (new Date().setHours(0, 0, 0, 0) == formattedDate)
        return 'Сегодня'
    else if (yesterday.setHours(0, 0, 0, 0) == formattedDate)
        return 'Вчера'
    else 
        return new Intl.DateTimeFormat('ru', {dateStyle: 'short', timeStyle: 'short'}).format(new Date(date))
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
    amount = amount.toString();
    const cleanedString = amount.replace(/[^\d.,]/g, '').replace(/\./g, ',')
    const parts = cleanedString.split('.')
    const decimalLmited = parts[1] ?  '.' + parts[1].slice(0, 2) : '';
    const mainPart = parts[0].replace(/\B(?=(\d{3})+(?!\d))/g, ' ')
    return mainPart + decimalLmited
}

export { URL_BASE, getData, client, cards, history, totalPages, showHistory, formatDate, processResponse, 
    formatAmount, showToast, renderTransaction, showClientLogin }