PLT Tools
=========

About
------------
The [HPGL (Hewlet-Packard Graphics Language)](http://en.wikipedia.org/wiki/HPGL) is rather old printer control language, originally designed for pen plotters. It is still used as input format (as *.PLT file) for eg. milling or engraver machines.

The purpose of this sofware is to do optimisation in sequence of lines to minimize the skips between line segements. The reason is, that programs exporting to HPGL, such as virtual printers, output the lines arbitrary and not logically at all, then the engraver machine spends more time skipping between lines than the engraving itself.

This is really beta-version doing only basic functions - simple view, simple optimization, simple export of optimized file. You may easily find a bug, eg. when trying to load *.PLT file of other type, than I have tested it with. Use on your own risk.

Installation
------------

1.  Make sure that you have installed Java runtime environment version 6 or higher. You can download from [www.java.com](http://www.java.com/en/download/manual.jsp?locale=en&host=www.java.com:80).
2.  Download the application - download whole content of ["dist" directory](https://github.com/fugazza/PLTtools/tree/master/dist) and copy it into your local directory.
3.  Run by clicking on PLTtools.jar.