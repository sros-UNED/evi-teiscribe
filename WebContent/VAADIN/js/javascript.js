/**
 * 
*/

document.addEventListener("paste", function(e) {
	if (document.getElementById("TextEditor").compareDocumentPosition(e.target) & Node.DOCUMENT_POSITION_CONTAINED_BY) {
		e.preventDefault();
		if ((!isNaN(e.target.id)) && (parseInt(Number(e.target.id)) == e.target.id) && (!isNaN(parseInt(e.target.id, 10)))) {
			if (e.clipboardData) {
				content = (e.originalEvent || e).clipboardData.getData('text/plain').replace(/[^\u0020-\u007E\u00A1-\uFFFF]/gmi, "");
				document.execCommand('insertText', false, content);
			} else if (window.clipboardData) {
				content = window.clipboardData.getData('Text').replace(/[^\u0020-\u007E\u00A1-\uFFFF]/gmi, "");//.replace(/[^\x20-\xFF]/gmi, "");
				document.selection.createRange().pasteHTML(content);
			}
		}
	}
	
});