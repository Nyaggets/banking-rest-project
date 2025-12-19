class ClientRequest {
    constructor(phone, username, password){
        this.phone = phone,
        this.username = username,
        this.password = password
    }
}

const phone = document.getElementById('phone')
const username = document.getElementById('username')
const password = document.getElementById('password')

const loginButton = document.getElementById('auth-button')
loginButton.addEventListener('click', async function() {
    const newClient = new ClientRequest(phone, username, password)

    let response = await fetch('http://localhost:8080/login', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json;charset=utf-8'
        },
        body: JSON.stringify(newClient)
    })

    let result = await response.json()
    console.log(result.status)
})