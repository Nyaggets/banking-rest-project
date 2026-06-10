import { showSpinner } from '/js/utils/sharedFunctions.js'

showSpinner()

if (new URLSearchParams(window.location.search).has('error')) {
    const error = document.getElementById('error-msg')
    error.hidden = false
    error.innerText = "Неверный логин или пароль"
}