import { showSpinner, processResponse } from '/js/utils/sharedFunctions.js'

showSpinner()

const authForm = document.getElementById('auth-form')
authForm.addEventListener('submit', async (e) => {
    e.preventDefault()

    const formData = new FormData(authForm)
    const response = await fetch('/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: new URLSearchParams({
            username: formData.get('username'),
            password: formData.get('password'),
        }),
    })
    if (response.ok)
        window.location.assign("/main")
    else
        processResponse(response)
})