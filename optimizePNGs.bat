@echo off
oxipng -o max --strip safe --zopfli --recursive "%~dp0src\main\resources\assets"
