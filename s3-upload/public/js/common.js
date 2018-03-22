/**
 * 모달닫을때 모든 입력값 reset
 */
$('.modal').on('hidden.bs.modal', function (e) {
    console.log('modal close');
  $(this).find('form')[0].reset()
});