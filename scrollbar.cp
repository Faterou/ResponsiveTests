We say that there is an horizontal scrollbar when (
	the page's width is less than the page's scroll-width
).

"""
  @name Horizontal scrollbar is a no-no
  @description There should never be an horizontal scrollbar
  @severity Error
"""
Always (
	Not ( there is an horizontal scrollbar )
).