function saveCollapseState() {
  var menus = document.querySelectorAll('nav#menu .submenu');
  var expandedMenus = [];
  Array.prototype.forEach.call(menus, function (menu, index) {
    if (menu.className.indexOf('collapsed') == -1) {
      expandedMenus.push(index.toString());
    }
  });
  document.cookie = "nav_state=" + expandedMenus.join(',');
}

function getCollapseState() {
  var cname = "nav_state=";
  var cookies = document.cookie.split(';');
  for (var i = 0; i < cookies.length; i++) {
    var c = cookies[i].trim();
    if (c.indexOf(cname) == 0) {
      return c.substring(cname.length, c.length).split(',');
    }
  }
  return [];
}

document.onreadystatechange = function () {
  switch (document.readyState) {
    case 'interactive':
      var menus = document.querySelectorAll('nav#menu .devsite-nav-section');
      var nav_state = getCollapseState();
      Array.prototype.forEach.call(menus, function (menu, index) {
        var anchor = menu.querySelector('[name=subheader]');
        var list = menu.querySelector('[name=list]');
        if (nav_state.indexOf(index.toString()) > -1) {
          menu.classList.toggle('collapsed', false);
          var active = true;
        } else {
          var active = list.querySelector('.active');
          menu.classList.toggle('collapsed', ! active);
        }
        list.dataset.height = list.clientHeight + 'px';
        list.style.maxHeight = active ? list.dataset.height : '0px';
        anchor.addEventListener('click', function() {
          list.style.maxHeight = (list.style.maxHeight == '0px') ? list.dataset.height : '0px';
          menu.classList.toggle('collapsed');
          saveCollapseState();
        });
      });
      break;
    case 'complete':
      document.querySelector('nav#menu').classList.add('animate');
      break;
  }
};
