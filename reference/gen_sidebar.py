

def recur(data, delimiter=''):
	for x in data:
		print delimiter + x[0], x[1]
		if x[2]:
		  recur(x[2], delimiter + '  ')

def buildSidebar(data):
	print '<script>'
	print '{% include nav.js %}'
	print '</script>'
	print '<nav class="devsite-section-nav-responsive devsite-nav" tabindex="0" style="left: -256px;">'
	print '<ul class="devsite-nav-expandable">'
	processTree(data)
	print '</ul>'
	print '</nav>'
	print '<div class="devsite-main-content clearfix" style="margin-top: 40px;">'
	print '<nav class="devsite-section-nav devsite-nav" style="left: auto; max-height: 737px; position: relative; top: 0px;">'
	print '<ul class="devsite-nav-expandable">'
	processTree(data)
	print '</ul>'
	print '</nav>'
	print '<nav class="devsite-page-nav devsite-nav" style="position: relative; left: auto; max-height: 737px; top: 0px;"></nav>'

def processTree(data,delimiter = ''):
	for x in data:
		if x[2]:
			print delimiter + '<li class="devsite-nav-item devsite-nav-item-section-expandable">'
			print delimiter + '<a class="devsite-nav-title devsite-nav-title-no-path " tabindex="0">'
			print delimiter + '<span>{}</span></a>'.format(x[0])
			print delimiter + '<a class="devsite-nav-toggle devsite-nav-toggle-collapsed material-icons"></a>'
			print delimiter + '<ul class="devsite-nav-section devsite-nav-section-collapsed">'
			if x[1]:
				processTree([["Overview", x[1], None]], delimiter + '  ')
			processTree(x[2], delimiter + '  ')
			print delimiter + '</ul></li>'
		else:
			print delimiter + '<li class="devsite-nav-item">'
			if(x[1]):
				print delimiter + '<a href="{{site_root}}%s" class="devsite-nav-title gc-analytics-event">'% x[1]
			print delimiter + '<span>{}</span>'.format(x[0]),
			if(x[1]):
				print '</a></li>'
			else:
				print '</li>'

f = None
with open('navtree_data.js') as file:
  f = file.read()
exec(filter(lambda x: x != "\n", f[4:]).replace(" null", " None"))

buildSidebar(NAVTREE_DATA)