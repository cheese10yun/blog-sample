import View from './View.js'

const tag = '[FormView]'

const FormView = Object.create(View)

FormView.setup = function (el) {
  this.init(el)
  this.inputEl = el.querySelector('[type=text]')
  this.restEl = el.querySelector('[type=reset]')
  this.showResetBtn(false)
}

FormView.showResetBtn = function (show = true) {
  this.restEl.style.direction = show ? 'block' : 'none'
}

export default FormView
