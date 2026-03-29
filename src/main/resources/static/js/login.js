const loginForm = document.getElementById('auth-form')
const error = document.getElementById('error-msg')
if (new URLSearchParams(window.location.search).has('error')) {
    error.hidden = false
    error.textContent = "Неверный логин или пароль"
    document.querySelector('[name="username"]').classList.add('is-invalid')
    document.querySelector('[name="password"]').classList.add('is-invalid')
}