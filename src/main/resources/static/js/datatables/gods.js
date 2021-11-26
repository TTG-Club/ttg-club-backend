$(document).ready(function() {
	var scrollEventHeight = 0;
	var rowSelectIndex = 0;
	var table = $('#gods').DataTable({
		ajax : '/data/gods',
		dom: 't',
		serverSide : true,
        deferRender: true,
		iDisplayLength : 25,
        scrollCollapse: true,
		select: true,
		select: {
			style: 'single'
		},
        searchPanes: {
            initCollapsed: true,
            viewCount: false,
            dtOpts: {
                select: {
                    //style: 'multi'
                },
				searching: false,
            },
			orderable: false
        },
		columns : [
		{
			data : "name",
			render : function(data, type, row) {
				if (type === 'display') {
					var result ='<div class="spell_lvl">' + row.aligmentShort + '</div>';
					result+='<div class="spell_name">' + row.name;
					result+='<span>' + row.englishName + '</span></div>';
					result+='<div class="spell_school">' + row.commitment + '</div>';
					return result;
				}
				return data;
			}
		}, 
		{
			data : 'englishName',
		},
		{
			data : 'alignment',
			searchable: false,
		},
		{
			data : 'domains',
			searchable: false,
		},
		{
			data : 'rank',
			searchable: false,
		},
		{
			data : 'sex',
			searchable: false,
		},
		],
		columnDefs : [
		{
			"targets": [ 1, 2, 3, 4, 5 ],
			"visible": false
		},
		],
		buttons: [
		{
		}],
		order : [[0, 'asc']],
			language : {
				processing : "Загрузка...",
				searchPlaceholder: "Поиск ",
				search : "_INPUT_",
				lengthMenu : "Показывать _MENU_ записей на странице",
				zeroRecords : "Ничего не найдено",
				info : "Показано _TOTAL_",
				infoEmpty : "Нет доступных записей",
				infoFiltered : "из _MAX_",
				loadingRecords: "Загрузка...",
		        searchPanes: {
		            title: {
		                 _: 'Выбрано фильтров - %d',
		                 0: 'Фильтры не выбраны',
		                 1: 'Один фильтр выбран'
		            },
	                collapseMessage: 'Свернуть все',
	                showMessage: 'Развернуть все',
	                clearMessage: 'Сбросить фильтры'
		        }
		},
		initComplete: function(settings, json) {
		    $('#gods tbody tr:eq(0)').click();
		    table.row(':eq(0)', { page: 'current' }).select(); 
			scrollEventHeight = document.getElementById('scroll_load_simplebar').offsetHeight - 300;
		    const simpleBar = new SimpleBar(document.getElementById('scroll_load_simplebar'));
		    simpleBar.getScrollElement().addEventListener('scroll', function(event){
		    	if (simpleBar.getScrollElement().scrollTop > scrollEventHeight){
		    	      table.page.loadMore();
		    	      simpleBar.recalculate();
		    	      scrollEventHeight +=750;
		    	}
		    });
		    table.searchPanes.container().prependTo($('#searchPanes'));
		    table.searchPanes.container().hide();
		},
		drawCallback: function ( settings ) {
		    $('#gods tbody tr:eq(0)').click();
		    table.row(':eq(0)', { page: 'current' }).select();
		}
	});
	$('#gods tbody').on('click', 'tr', function () {
		if(!document.getElementById('list_page_two_block').classList.contains('block_information')){
			document.getElementById('list_page_two_block').classList.add('block_information');
		}
		var tr = $(this).closest('tr');
		var table = $('#gods').DataTable();
		var row = table.row( tr );
		var data = row.data();
		document.getElementById('god_name').innerHTML = data.name;
		document.getElementById('alignment').innerHTML = data.alignment;
		document.getElementById('rank').innerHTML = data.rank;
		document.getElementById('title').innerHTML = data.nicknames;
		document.getElementById('symbol').innerHTML = data.symbol;
		document.getElementById('domains').innerHTML = data.domains;
		document.getElementById('pantheon').innerHTML = data.pantheon;

		var source = '<span class="tip" data-tipped-gods="inline: \'tooltip-race-source-' + data.id +'\'">' + data.bookshort + '</span>';
		source+= '<span id="tooltip-race-source-'+ data.id + '" style="display: none">' + data.book + '</span>';
		document.getElementById('source').innerHTML = source;
		document.title = data.name;
		history.pushState('data to be passed', '', '/gods/' + data.englishName.split(' ').join('_'));
		var url = '/gods/fragment/' + data.id;
		$("#content_block").load(url);
	});
	$('#search').on( 'keyup click', function () {
		if($(this).val()){
			$('#text_clear').show();
		}
		else {
			$('#text_clear').hide();
		}
		table.tables().search($(this).val()).draw();
	});
	$('#btn_filters').on('click', function() {
		var table = $('#gods').DataTable();
		table.searchPanes.container().toggle();
	});
});
$('#text_clear').on('click', function () {
	$('#search').val('');
	const table = $('#gods').DataTable();
	table.tables().search($(this).val()).draw();
	$('#text_clear').hide();
});
$('#btn_close').on('click', function() {
	document.getElementById('list_page_two_block').classList.remove('block_information');
});