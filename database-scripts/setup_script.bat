@echo off
REM E-Insurance Project Structure Setup Script (Windows)
REM This script creates the complete directory structure for all microservices

echo Creating E-Insurance Microservices Project Structure...
echo.

REM Create main project directory
mkdir e-insurance-parent 2>nul
cd e-insurance-parent

REM Create documentation directories
mkdir docs\api 2>nul
mkdir docs\postman 2>nul
mkdir docs\diagrams 2>nul

REM Common Library Module
echo Setting up common-lib...
mkdir common-lib\src\main\java\com\einsurance\common\dto 2>nul
mkdir common-lib\src\main\java\com\einsurance\common\exception 2>nul
mkdir common-lib\src\main\java\com\einsurance\common\config 2>nul
mkdir common-lib\src\main\java\com\einsurance\common\util 2>nul
mkdir common-lib\src\main\java\com\einsurance\common\security 2>nul
mkdir common-lib\src\main\resources 2>nul
mkdir common-lib\src\test\java\com\einsurance\common 2>nul

REM User Service
echo Setting up user-service...
mkdir user-service\src\main\java\com\einsurance\user\controller 2>nul
mkdir user-service\src\main\java\com\einsurance\user\service 2>nul
mkdir user-service\src\main\java\com\einsurance\user\repository 2>nul
mkdir user-service\src\main\java\com\einsurance\user\entity 2>nul
mkdir user-service\src\main\java\com\einsurance\user\dto 2>nul
mkdir user-service\src\main\java\com\einsurance\user\mapper 2>nul
mkdir user-service\src\main\java\com\einsurance\user\config 2>nul
mkdir user-service\src\main\java\com\einsurance\user\security 2>nul
mkdir user-service\src\main\resources\db\migration 2>nul
mkdir user-service\src\main\resources\templates 2>nul
mkdir user-service\src\test\java\com\einsurance\user\controller 2>nul
mkdir user-service\src\test\java\com\einsurance\user\service 2>nul
mkdir user-service\src\test\java\com\einsurance\user\repository 2>nul

REM Policy Service
echo Setting up policy-service...
mkdir policy-service\src\main\java\com\einsurance\policy\controller 2>nul
mkdir policy-service\src\main\java\com\einsurance\policy\service 2>nul
mkdir policy-service\src\main\java\com\einsurance\policy\repository 2>nul
mkdir policy-service\src\main\java\com\einsurance\policy\entity 2>nul
mkdir policy-service\src\main\java\com\einsurance\policy\dto 2>nul
mkdir policy-service\src\main\java\com\einsurance\policy\mapper 2>nul
mkdir policy-service\src\main\java\com\einsurance\policy\config 2>nul
mkdir policy-service\src\main\resources\db\migration 2>nul
mkdir policy-service\src\main\resources\templates 2>nul
mkdir policy-service\src\test\java\com\einsurance\policy\controller 2>nul
mkdir policy-service\src\test\java\com\einsurance\policy\service 2>nul
mkdir policy-service\src\test\java\com\einsurance\policy\repository 2>nul

REM Payment Service
echo Setting up payment-service...
mkdir payment-service\src\main\java\com\einsurance\payment\controller 2>nul
mkdir payment-service\src\main\java\com\einsurance\payment\service 2>nul
mkdir payment-service\src\main\java\com\einsurance\payment\repository 2>nul
mkdir payment-service\src\main\java\com\einsurance\payment\entity 2>nul
mkdir payment-service\src\main\java\com\einsurance\payment\dto 2>nul
mkdir payment-service\src\main\java\com\einsurance\payment\mapper 2>nul
mkdir payment-service\src\main\java\com\einsurance\payment\config 2>nul
mkdir payment-service\src\main\java\com\einsurance\payment\stripe 2>nul
mkdir payment-service\src\main\