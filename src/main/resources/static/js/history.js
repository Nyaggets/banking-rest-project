import { URL_BASE, client, history, totalPages, getData, cards } from './utils/getData.js'
import { showHistory } from './utils/processData.js'

const url = new URLSearchParams(window.location.search)
let pagesCount = totalPages
const historyList = document.getElementById('history-list')
const typeSelect = document.getElementById('type-select')

const paginationContainer = document.getElementById('btns')
const prevLink = document.getElementById('prev-link')
const nextLink = document.getElementById('next-link')

const currentParams = {
    page: url.get('page'),
    cardId: url.get('cardId'),
    type: url.get('type'),
    start: url.get('start'),
    end: url.get('end')        
}
const NotNullParams = () => Object.fromEntries(Object.entries(currentParams).filter(([key, value]) => value != null))

const reloadPage = async () => {
    const currentServerUtl = buildUrlWithParams(`${URL_BASE}/api/history`, NotNullParams())
    const { content: paginationList, totalPages: newTotalPages } = await getData(currentServerUtl)
    pagesCount = newTotalPages
    const msgElem = document.getElementById('history-msg')
    if (paginationList.length == 0) {
        msgElem.hidden = false
        const title = document.createElement('h3')
        title.innerText = "Транзакции не найдены"
        const subtitle = document.createElement('p')
        subtitle.innerText = 'Сбросьте или попробуйте другие фильтры'
        subtitle.classList.add('caption')
        msgElem.appendChild(title)
        msgElem.appendChild(subtitle)
    }
    else { 
        msgElem.hidden = true
        msgElem.innerHTML = ''
    }
    showHistory(paginationList, historyList)
    generatePagination()
    const currentUtl = buildUrlWithParams(`${URL_BASE}/history`, NotNullParams())
    window.history.pushState(NotNullParams(), '', currentUtl)
}   

const buildUrlWithParams = (baseUrl) => {
    const newUrl = new URL(baseUrl)
    Object.entries(NotNullParams()).forEach(([key, value]) => {
        newUrl.searchParams.set(key, value)
    })
    return newUrl
}

const btnLinkBaseUrl = new URL(`${URL_BASE}/api/history`)
const generatePagination = () => {
    if (pagesCount == 0) {
        document.getElementById('pagination-nav').hidden = true
        return
    }
    document.getElementById('pagination-nav').hidden = false
    paginationContainer.innerHTML = ''
    for (let i = 0; i < pagesCount; i++) {
        const li = document.createElement('li')
        li.classList.add('page-item')
        const link = document.createElement('a')
        link.classList.add('page-link', 'page-number-link')
        link.dataset.page = i
        link.textContent = i + 1

        li.appendChild(link)
        paginationContainer.appendChild(li)
    }

    document.querySelectorAll('.page-number-link').forEach(btn => {
        const pageState = { ...currentParams, page: btn.dataset.page }
        const cleanState = Object.fromEntries(Object.entries(pageState).filter(([_, v]) => v != null))
        Object.entries(cleanState).forEach(([k, v]) => btnLinkBaseUrl.searchParams.set(k, v))
        btn.href = btnLinkBaseUrl
    })
}

paginationContainer.addEventListener('click', async (e) => {
    e.preventDefault()
    const target = e.target.closest('.page-number-link')
    if (!target)
        return

    currentParams.page = e.target.dataset.page
    reloadPage()
})

typeSelect.addEventListener('change', async () => {
    currentParams.page = 0
    if (!typeSelect.value)
        currentParams.type = null
    else
        currentParams.type = typeSelect.value
    reloadPage()
})

const cardSelect = document.getElementById('counterparty-select')
cards.forEach((card, key) => {
    cardSelect.add(new Option(`${card.hiddenNumber} ${card.balance}₽`, card.id, false, (card.id == currentParams.cardId)))
})
cardSelect.addEventListener('change', async () => {
    currentParams.page = 0
    const url = new URLSearchParams(window.location.search)
    const currentUtl = new URL(`${URL_BASE}/history`)
    if (!cardSelect.value)
        currentParams.cardId = null
    else
        currentParams.cardId = cardSelect.value
    reloadPage()
})


const dateRangeInput = document.getElementById('date-range-picker')
const fp = flatpickr(dateRangeInput, {
    mode: "range",
    dateFormat: "Y-m-d",
    closeOnSelect: false,    
    maxDate: "today",   
    locale: {
        weekdays: {
            shorthand: ['Вс', 'Пн', 'Вт', 'Ср', 'Чт', 'Пт', 'Сб'],
            longhand: ['Воскресенье', 'Понедельник', 'Вторник', 'Среда', 'Четверг', 'Пятница', 'Суббота']
        },
        months: {
            shorthand: ['Янв', 'Фев', 'Мар', 'Апр', 'Май', 'Июн', 'Июл', 'Авг', 'Сен', 'Окт', 'Ноя', 'Дек'],
            longhand: ['Январь', 'Февраль', 'Март', 'Апрель', 'Май', 'Июнь', 'Июль', 'Август', 'Сентябрь', 'Октябрь', 'Ноябрь', 'Декабрь']
        },
        firstDayOfWeek: 1,
        rangeSeparator: " — ",
        weekAbbreviation: "Нед."
    },
    static: true,
    onReady: (selectedDates, dateStr, instance) => {
        if (instance.calendarContainer.querySelector('#flatpickr-confirm-btn')) 
            return
        const confirmBtn = document.createElement('button')
        confirmBtn.textContent = 'Применить период'
        confirmBtn.classList.add('main-btn', 'mb-2')
        confirmBtn.id = 'flatpickr-confirm-btn'
        confirmBtn.type = 'button'

        confirmBtn.addEventListener('click', async () => {
            if (instance.selectedDates.length >= 1) {
                const startDate = instance.selectedDates[0].toISOString().split('T')[0]
                const endDate = instance.selectedDates.length == 2 
                    ? instance.selectedDates[1].toISOString().split('T')[0] 
                    : startDate

                currentParams.start = startDate
                currentParams.end = endDate
                currentParams.page = 0
                reloadPage()
                instance.close()
            }
        })
        instance.calendarContainer.appendChild(confirmBtn)
    }
})

const periodSelect = document.getElementById('period-select')
periodSelect.addEventListener('change', () => {
    currentParams.page = 0
    if (periodSelect.value == 'PERIOD') {
        fp.open()
        periodSelect.value = ''
        return
    } 
    const today = new Date()
    currentParams.end = today.toISOString().split('T')[0]
    switch (periodSelect.value) {
        case 'WEEK':
            today.setDate(today.getDate() - 7)
            currentParams.start = today.toISOString().split('T')[0]
            break
        case 'MONTH':
            today.setMonth(today.getMonth() - 1)
            currentParams.start = today.toISOString().split('T')[0]
            break
        case 'SIX_MONTHS':
            today.setMonth(today.getMonth() - 6)
            currentParams.start = today.toISOString().split('T')[0]
            break
        default:
            currentParams.start = null
            currentParams.end = null
            break
    }
    reloadPage()
})


reloadPage()