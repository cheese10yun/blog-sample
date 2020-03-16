import FormView from "../views/FormView";

const tag = '[MainController]'

export default {
  init() {
    console.log(tag, 'init()')
    FormView.setup(document.querySelector("form"))
  }
}