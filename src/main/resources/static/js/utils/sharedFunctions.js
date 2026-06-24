const formatDate = (date) => {
    const formattedDate = new Date(date).setHours(0, 0, 0, 0)
    const yesterday = new Date(new Date().setDate(new Date().getDate() - 1))
    if (new Date().setHours(0, 0, 0, 0) == formattedDate)
        return `Сегодня, ${new Intl.DateTimeFormat('ru', {timeStyle: 'short'}).format(new Date(date))}`
    else if (yesterday.setHours(0, 0, 0, 0) == formattedDate)
        return `Вчера, ${new Intl.DateTimeFormat('ru', {timeStyle: 'short'}).format(new Date(date))}`
    else
        return new Intl.DateTimeFormat('ru', {dateStyle: 'short', timeStyle: 'short'}).format(new Date(date))
}

const formatAmount = (amount) => {
    if (amount == null || amount === '') 
        return ''

    const cleaned = amount.toString().replace(/[^\d.,]/g, '').replace(/\./g, ',')
    const separatorIndex = cleaned.indexOf(',')
    let mainPart = separatorIndex !== -1 
        ? cleaned.substring(0, separatorIndex) 
        : cleaned
    let decimalPart = separatorIndex !== -1 
        ? cleaned.substring(separatorIndex + 1).replace(/[^\d]/g, '') 
        : ''
    
    if (mainPart.length > 1 && !(mainPart === '0' && decimalPart)) 
        mainPart = mainPart.replace(/^0+/, '') || '0'
    
    const formatted = mainPart.replace(/\B(?=(\d{3})+(?!\d))/g, ' ')
    const result = decimalPart ? `${formatted},${decimalPart}` : formatted
    
    return cleaned.endsWith(',') ? result + ',' : result
}

const formatPhoneOrCard = (value) => {
    if (value == null || value == undefined)
        return ''
    
    let valueDigits = value.replace(/\D/g, '')
    if (valueDigits.length >= 16)
        return valueDigits.match(/.{1,4}/g)?.join(' ') || valueDigits;
    if (valueDigits.length > 11)
        return valueDigits
    if (valueDigits.length == 0)
        return ''

    let formatted = valueDigits.charAt(0)
    if (valueDigits.length > 1) 
        formatted += ' ' + valueDigits.substring(1, 4)
    if (valueDigits.length > 4) 
        formatted += ' ' + valueDigits.substring(4, 7)
    if (valueDigits.length > 7) 
        formatted += ' ' + valueDigits.substring(7, 9)
    if (valueDigits.length > 9) 
        formatted += ' ' + valueDigits.substring(9, 11)

    return formatted
}

const createNewElement = (elTag, elClasses, elInnerHTML) => {
    const element = document.createElement(elTag.toLowerCase())
    element.className = elClasses
    element.innerHTML = elInnerHTML ?? ''
    return element
}

const formatCardOption = (card) => `${card.hiddenNumber} — ${formatAmount(card.balance)}₽`

const showSpinner = () => {
    const spinner = document.getElementById('spinner')
    if (!spinner)
        return

    let timer = setTimeout(() => {
        spinner.style.display = 'none'
        spinner.remove()
    }, 200)

    if (document.readyState === 'loading') {
        window.addEventListener('DOMContentLoaded', () => {
            clearTimeout(timer)
            timer = setTimeout(() => {
                spinner.style.display = 'none'
                spinner.remove()
            }, 50)
        })
    }
    
    setTimeout(() => {
        if (spinner.parentNode) {
            spinner.style.display = 'none'
            spinner.remove()
        }
    }, 5000)
}

const showToast = (title, description) => {
    const toastInstance = new bootstrap.Toast(document.querySelector('.toast'), { delay: 1900, autohide: true })
    document.getElementById('toast-header').innerText = title
    document.getElementById('toast-body').innerHTML = description
    toastInstance.show()
}

const processResponse = async (response) => {
    const errorData = await response.json()
    const { code, errors = {}, expiresAt } = errorData
    switch (response.status) {
        case 500:
            showToast('Ошибка сервера', 'Запрос не может быть выполнен. Попробуйте позже')
            break

        case 401:
            sessionStorage.clear()
            showToast('Сессия завершена', 'Время сессии истекло, повторите вход')
            setTimeout(() => window.location.assign('/session-expired'), 1900)
            break

        case 429: {
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
        }

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
    if (response.ok)
        return response.json()
    else
        processResponse(response)
}

export { formatDate, formatAmount, formatCardOption, formatPhoneOrCard, showSpinner, showToast, createNewElement, processResponse, getData }