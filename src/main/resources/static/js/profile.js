import { client, URL_BASE, API_BASE, showClientLogin } from './utils/sharedData.js'
import { showToast, showSpinner, processResponse } from './utils/sharedFunctions.js'

showSpinner()
showClientLogin()

const phoneInput = document.getElementById('phone')
const loginInput = document.getElementById('login')
const dataForm = document.getElementById('data-form')
const passwordForm = document.getElementById('password-form')
const saveDataBtn = document.getElementById('change-data-btn')
const savePasswordBtn = document.getElementById('change-password-btn')
const passportRevealBtn = document.getElementById('passport-collapse-btn')
const passportCollapseEl = document.getElementById('passport-collpase')

phoneInput.value = client.phone
loginInput.value = client.login
document.getElementById('data-form').addEventListener('submit', async (e) => {
    e.preventDefault()
    const body = {
        login: loginInput.value.trim(),
        phone: phoneInput.value.trim()
    }   
    if (body.login == client.login && body.phone == client.phone)
        return

    const response = await fetch(`${API_BASE}/clients/profile`, {
        method: 'PATCH',
        headers: { 'Content-Type': 'application/json;charset=utf-8' },
        body: JSON.stringify(body)
    })
    if (response.ok) {
        client.phone = body.phone
        client.login = body.login
        showClientLogin()
        showToast('Обновление профиля', 'Изменения сохранены')
    } else 
        processResponse(response)
    saveDataBtn.blur()
})

document.getElementById('password-form').addEventListener('submit', async (e) => {
    e.preventDefault()
    const body = Object.fromEntries(new FormData(passwordForm).entries())
    const response = await fetch(`${API_BASE}/clients/password`, {
        method: 'PATCH',
        headers: { 'Content-Type': 'application/json;charset=utf-8' },
        body: JSON.stringify(body)
    })
    if (response.ok) {
        showToast('Успешно выполнено', 'Данные изменены')
        passwordForm.reset()
        bootstrap.Collapse.getInstance('#passwordCollapse').hide()
        showToast('Обновление профиля', 'Изменения сохранены')
    } else 
        processResponse(response)
})

const modalEl = document.getElementById('confirm-modal')
const modalInput = document.getElementById('password-input')
const modalConfirmBtn = document.getElementById('password-conf-btn')

passportRevealBtn.addEventListener('click', (e) => {
    e.preventDefault()
    if (passportCollapseEl.classList.contains('show')) {
        bootstrap.Collapse.getOrCreateInstance(passportCollapseEl, { toggle: false }).hide()
        passportRevealBtn.innerText = 'Показать'
        return
    }
    bootstrap.Modal.getOrCreateInstance(modalEl).show()
})

modalEl.addEventListener('hidden.bs.modal', () => {
    modalInput.value = ''
})

const modalForm = document.getElementById('password-conf-form')
modalForm.addEventListener('submit', async (e) => {
    e.preventDefault()
    const password = modalInput.value.trim()
    const response = await fetch(`${API_BASE}/clients/reveal-passport`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json;charset=utf-8' },
        body: JSON.stringify({ password })
    })

    if (response.ok) {
        const data = await response.json()
        console.log(data)
        document.getElementById('series').innerText = data.series || ''
        document.getElementById('number').innerText = data.number || ''
        document.getElementById('full-name').innerText = data.fullName || ''
        document.getElementById('department-code').innerText = data.departmentCode || ''
        document.getElementById('issued-by').innerText = data.issuedBy || ''
        document.getElementById('issue-date').innerText = new Date(data.issueDate).toLocaleDateString() || ''

        const bsCollapse = bootstrap.Collapse.getOrCreateInstance(passportCollapseEl, { toggle: false })
        bsCollapse.show()
        passportRevealBtn.innerText = 'Скрыть'
        modalForm.reset()
        const modalInstance = bootstrap.Modal.getInstance(modalEl)
        modalInstance.hide()
    } else 
        processResponse(response)
})