const URL_BASE = 'http://localhost:8080';

const getData = async(url) => {
    const response = await fetch(url);
    if (response.ok) {
        return response.json()
    }
    else {
        const errorMap = await response.json()
        Object.entries(errorMap).forEach(([field, message]) => {
            document.querySelector(`[name="${field}"]`).classList.add('error')
            document.querySelector(`[data-error-for="${field}"]`).textContent = message
        })
    }
}

const client = await getData(`${URL_BASE}/clients/me`)
const cards = Array.from(await getData(`${URL_BASE}/clients/${client.id}/cards`))
const { content: history, totalPages} = await getData(`${URL_BASE}/api/history`)

export { URL_BASE, getData, client, cards, history, totalPages }