import { createApp } from 'vue'
import './style.css'
import App from './App.vue'
import router from './router'

// Global Error Handling for Debugging
window.onerror = function(message, source, lineno, colno, error) {
  const errorDiv = document.createElement('div');
  errorDiv.style.position = 'fixed';
  errorDiv.style.top = '0';
  errorDiv.style.left = '0';
  errorDiv.style.width = '100%';
  errorDiv.style.backgroundColor = 'red';
  errorDiv.style.color = 'white';
  errorDiv.style.zIndex = '9999';
  errorDiv.style.padding = '20px';
  errorDiv.innerText = `Error: ${message}\nSource: ${source}:${lineno}:${colno}\nStack: ${error ? error.stack : 'N/A'}`;
  document.body.appendChild(errorDiv);
};

window.onunhandledrejection = function(event) {
  const errorDiv = document.createElement('div');
  errorDiv.style.position = 'fixed';
  errorDiv.style.bottom = '0';
  errorDiv.style.left = '0';
  errorDiv.style.width = '100%';
  errorDiv.style.backgroundColor = 'orange';
  errorDiv.style.color = 'black';
  errorDiv.style.zIndex = '9999';
  errorDiv.style.padding = '20px';
  errorDiv.innerText = `Unhandled Rejection: ${event.reason}`;
  document.body.appendChild(errorDiv);
};

try {
  const app = createApp(App)
  app.use(router)
  app.mount('#app')
} catch (err) {
  console.error('Mount Error:', err)
  window.onerror(err.message, 'main.js', 0, 0, err)
}
