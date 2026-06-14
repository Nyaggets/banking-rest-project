import { URL_BASE, API_BASE, totalPages, cards, showHistory, showClientLogin } from '/js/utils/sharedData.js'
import { formatAmount, showSpinner, createNewElement, getData } from '/js/utils/sharedFunctions.js'

showSpinner()
showClientLogin()
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
    types: url.getAll('types'),
    start: url.get('start'),
    end: url.get('end')        
}
const getCleanParams = () => Object.fromEntries(Object.entries(currentParams).filter(([key, value]) => 
    value != null && (!Array.isArray(value) || value.length > 0)))

const formatShortDate = (date) => new Intl.DateTimeFormat('ru', {dateStyle: 'short'}).format(new Date(date))
const selectedPeriodEl = document.getElementById('selected-period')
const showSelectedPeriod = () => {
    const params = getCleanParams()
    if (params.start && params.end)
        selectedPeriodEl.innerHTML = `Выбранный период: ${formatShortDate(params.start)} — ${formatShortDate(params.end)}`
    else
        selectedPeriodEl.innerHTML = ''
}

const buildUrlWithParams = (baseUrl) => {
    const newUrl = new URL(baseUrl)
    Object.entries(getCleanParams()).forEach(([key, value]) => {
        if (Array.isArray(value))
            value.forEach(v => newUrl.searchParams.append(key, v))
        else
            newUrl.searchParams.set(key, value)
    })
    return newUrl
}

const clearFilters = () => {
    Object.keys(currentParams).forEach(key => currentParams[key] = null)
    showSelectedPeriod.innerHTML = ''
    document.getElementById('counterparty-select').value = ''
    document.getElementById('type-select').value = ''
    document.getElementById('period-select').value = ''
    fp?.clear()
    loadPage()
}

const loadPage = async () => {
    const currentServerUrl = buildUrlWithParams(`${API_BASE}/cards/history`, getCleanParams())
    const { content: paginationList, totalPages: newTotalPages } = await getData(currentServerUrl)
    pagesCount = newTotalPages
    const msgElem = document.getElementById('history-msg')
    showSelectedPeriod()
    msgElem.innerHTML = ''
    if (paginationList.length == 0) {
        selectedPeriodEl.innerHTML = ''
        msgElem.hidden = false
        const title = createNewElement('h3', '', 'Транзакции не найдены')
        const subtitle = createNewElement('p', 'caption', 'Сбросьте или попробуйте другие фильтры')
        const btn = createNewElement('button', 'main-btn mt-4', 'Сбросить фильтры')
        btn.id = 'clear-filters-btn'
        btn.addEventListener('click', clearFilters)

        msgElem.appendChild(title)
        msgElem.appendChild(subtitle)
        msgElem.appendChild(btn)
    }
    else 
        msgElem.hidden = true
    
    showHistory(paginationList, historyList)
    generatePagination()
    const currentPageUrl = buildUrlWithParams(`${URL_BASE}/history`, getCleanParams())
    window.history.pushState(getCleanParams(), '', currentPageUrl)
}   

const generatePagination = () => {
    const navEl = document.getElementById('pagination-nav')
    if (pagesCount == 0) {
        navEl.hidden = true
        return
    }
    
    const currentPageNum = getCleanParams().page ?? '0'
    navEl.hidden = false
    paginationContainer.innerHTML = ''
    for (let i = 0; i < pagesCount; i++) {
        const li = createNewElement('li', 'page-item')
        const link = createNewElement('a', `page-link page-number-link ${i == currentPageNum ? 'acitve' : ''}`, i + 1)
        link.dataset.page = i

        li.appendChild(link)
        paginationContainer.appendChild(li)
    }

    document.querySelectorAll('.page-number-link').forEach(btn => {
        btn.href = buildUrlWithParams(`${URL_BASE}/history`, getCleanParams())
    })
}

paginationContainer.addEventListener('click', async (e) => {
    e.preventDefault()
    const target = e.target.closest('.page-number-link')
    if (!target)
        return

    currentParams.page = e.target.dataset.page
    loadPage()
})

const types = {
  'transfer': ['TRANSFER_OUT', 'TRANSFER_IN'],
  'deposit':    ['DEPOSIT', 'TRANSFER_IN'],
  'withdrawal':  ['WITHDRAWAL', 'TRANSFER_OUT']
}
typeSelect.addEventListener('change', async () => {
    currentParams.page = 0
    currentParams.types = typeSelect.value
        ? types[typeSelect.value] || []
        : null
    loadPage()
})

const cardSelect = document.getElementById('counterparty-select')
cards.forEach((card, key) => {
    cardSelect.add(new Option(`${card.hiddenNumber} — ${formatAmount(card.balance)}₽`, card.id, false, (card.id == currentParams.cardId)))
})
cardSelect.addEventListener('change', async () => {
    currentParams.page = 0
    currentParams.cardId = cardSelect.value
        ? cardSelect.value
        : null
    loadPage()
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
        const confirmBtn = createNewElement('button', 'main-btn, mb-2', 'Применить период')
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
                loadPage()
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
    loadPage()
})


loadPage()