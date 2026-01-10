import { client } from './utils/getData.js'

document.getElementById('client-name').innerHTML = client.name
document.getElementById('client-phone').innerHTML = client.phone