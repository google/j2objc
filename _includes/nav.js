document.documentElement.classList.add('js');

document.onreadystatechange = function () {
  switch (document.readyState) {
    case 'interactive':
      var groups = document.querySelectorAll('nav#menu .group');
      Array.prototype.forEach.call(groups, function (group) {
        var anchor = group.querySelector('[name=name]');
        var list = group.querySelector('[name=list]');
        var active = list.querySelector('.active');
        list.dataset.height = list.clientHeight + 'px';
        list.style.maxHeight = active ? list.dataset.height : '0px';
        group.classList.toggle('collapsed', ! active);
        anchor.addEventListener('click', function() {
          list.style.maxHeight = (list.style.maxHeight == '0px') ? list.dataset.height : '0px';
          group.classList.toggle('collapsed');
        });
      });
      break;
    case 'complete':
      // When the doc is fully loaded, turn on animation
      document.querySelector('nav#menu').classList.add('animate');
      break;
  }
};