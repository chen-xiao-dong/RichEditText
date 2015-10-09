# RichEditText
A rich edit text view on Android to replace native EditText view,it let user show/input more informational text.
The library support editting rich text on UI,like bold,different font color,add link for the selection and so on.

Comparing to a famous github project [cwac-richedit](https://github.com/commonsguy/cwac-richedit),which published by master 'commonsguy',this library is mainly 
focusing on editing process,for example,when you underline some segments but after that strike through the later part,how the former style is kept and get well with the next style impacting later words.





#Usage

### JCentor

This project has been pushed to JCentor [here](https://bintray.com/xiaodong666/maven/dach-richedit-android/view#) ,in 'aar'.To import the library in gradle:

`repositories {
     jcenter()
 }`

 with specified version:

 `compile 'com.earlyeast.android:RichEditText:0.2.3'`

### Supported Effects

At the time of this writing, here are all effects:

- `BOLD`
- `ITALIC`
- `UNDERLINE`
- `STRIKETHROUGH`
- `URL`
- `FOREGROUND` (font color)
- `BACKGROUND`

A folding-able toolbar is supplied to edit/switch the selection of text with different font styles.

### Editing 

- Status
The buttons on toolbar shows styles of current text segment with grey/normal.For example,the a word where cursor at is not bold,then the 'Bold' icon is gray.
- Edit
To edit font style,you need to select text first,then click relative icon to change its font style.For example,to make two words bold,first make the two words selected,
then click 'bold' to set that,if they are changed un-bold,click the 'bold' again to make the whole selection part bold.

### Demo screenshot

 The screen shots showing like [addlink] (https://raw.githubusercontent.com/eastearly/RichEditText/master/raw/screen-shot/addlink.png) and [changefontcolor](https://raw.githubusercontent.com/eastearly/RichEditText/master/raw/screen-shot/changefontcolor.png).

### Bugs found

1.When a segment was strike through,can not revoke by select the segment and click icon 'StrikeThrough',there is something not correct with the range logic.