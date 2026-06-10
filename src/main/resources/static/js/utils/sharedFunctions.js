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
    if (amount == null || amount == undefined)
        return ''
    amount = amount.toString()
    const amountDigits = amount.replace(/[^\d.,]/g, '').replace(/\./g, ',')
    const parts = amountDigits.split(',')
    const mainPart = parts[0].replace(/\B(?=(\d{3})+(?!\d))/g, ' ')
    const decimalLmited = parts[1] ? parts[1].slice(0, 2) : ''
    return decimalLmited ? `${mainPart},${decimalLmited}` : mainPart
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

const formatCardOption = (card) => `${card.hiddenNumber} — ${formatAmount(card.balance)}₽`

const showSpinner = () => {
    const spinner = document.getElementById('spinner')
    if (!spinner)
        return
    const hide = () => { 
        spinner.style.display = 'none' 
        spinner.remove() 
    }
    let timer = setTimeout(hide, 200)
    if (document.readyState === 'loading') {
        window.addEventListener('DOMContentLoaded', () => {
            clearTimeout(timer)
            timer = setTimeout(hide, 50)
        })
    }
    setTimeout(hide, 5000)
}

const showToast = (title, description) => {
    const toastInstance = new bootstrap.Toast(document.querySelector('.toast'), { delay: 1500, autohide: true })
    document.getElementById('toast-header').innerText = title
    document.getElementById('toast-body').innerHTML = description
    toastInstance.show()
}

export { formatDate, formatAmount, formatCardOption, formatPhoneOrCard, showSpinner, showToast }