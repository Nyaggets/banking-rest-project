import { client, URL_BASE, showToast, processResponse, showClientLogin } from './utils/sharedData.js'

showClientLogin()
const phoneInput = document.getElementById('phone')
const loginInput = document.getElementById('login')
const dataForm = document.getElementById('data-form')
const passwordForm = document.getElementById('password-form')
const saveDataBtn = document.getElementById('change-data-btn')
const savePasswordBtn = document.getElementById('change-password-btn')
const passportBtn = document.getElementById('passport-collapse-btn')
const passportCollapseEl = document.getElementById('passport-collpase')

passportBtn.addEventListener('click', () => {
  const isCollapseOpen = passportCollapseEl.classList.contains('show')

  if (isCollapseOpen) {
    bootstrap.Collapse.getOrCreateInstance(passportCollapseEl, { toggle: false }).hide()
    passportBtn.textContent = 'Показать'
  } else {
    passportBtn.innerText = 'Скрыть'
    bootstrap.Modal.getOrCreateInstance(modalEl).show()
  }
})

phoneInput.value = client.phone
loginInput.value = client.login

saveDataBtn.addEventListener('click', async () => {
    const body = {
        login: loginInput.value,
        phone: phoneInput.value
    }   
    const res = await fetch(`${URL_BASE}/clients/${client.id}/profile`, {
        method: 'PATCH',
        headers: { 'Content-Type': 'application/json;charset=utf-8' },
        body: JSON.stringify(body)
    })
    if (res.ok) {
        client.phone = body.phone
        client.login = body.login
        
    } else {
        const err = await res.json().catch(() => ({}))
        alert(err.message || 'Ошибка обновления')
    }
})

savePasswordBtn.addEventListener('click', async () => {
    console.log('uhvsckxl,' + Object.fromEntries(new FormData(passwordForm).entries()))
    const body = Object.fromEntries(new FormData(passwordForm).entries())
    const res = await fetch(`${URL_BASE}/clients/${client.id}/password`, {
        method: 'PATCH',
        headers: { 'Content-Type': 'application/json;charset=utf-8' },
        body: JSON.stringify(body)
    })
    if (res.ok) {
        showToast('Успешно выполнено', 'Данные изменены')
        passwordForm.reset()
        bootstrap.Collapse.getInstance('#passwordCollapse').hide()
    } else {
        const err = await res.json().catch(() => ({}))
        console.log(err.message || 'Ошибка смены пароля')
        showToast('Ошибка сохранения', 'Данные не изменены')
    }
})

const modalEl = document.getElementById('confirm-modal')
const modalInput = document.getElementById('password-input')
const modalConfirmBtn = document.getElementById('password-conf-btn')

modalEl.addEventListener('hidden.bs.modal', () => {
    modalInput.value = ''
})

modalConfirmBtn.addEventListener('click', async () => {
    const password = modalInput.value.trim()
    const result = await fetch(`${URL_BASE}/clients/reveal-passport`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json;charset=utf-8' },
        body: JSON.stringify({ password })
    })

    if (result.ok) {
        const data = await result.json()
        document.getElementById('series').innerText = data.series || ''
        document.getElementById('number').innerText = data.number || ''
        document.getElementById('department-code').innerText = data.departmentCode || ''
        document.getElementById('issued-by').innerText = data.issuedBy || ''
        document.getElementById('issue-date').innerText = data.issueDate || ''

        const bsCollapse = bootstrap.Collapse.getOrCreateInstance(passportCollapseEl, { toggle: false })
        bsCollapse.show()
        passportBtn.innerText = 'Скрыть'

        const modalInstance = bootstrap.Modal.getInstance(modalEl)
        modalInstance.hide()
    } else {
        processResponse(result)
    }
})