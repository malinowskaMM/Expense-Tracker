import * as React from 'react';
import { useTranslation } from "react-i18next";
import { List, ListItem, Box, Snackbar, Stack, Alert, AlertTitle, TextField, FormControl, ListItemText, ListItemButton, RadioGroup, FormControlLabel, Radio, Select, MenuItem, SelectChangeEvent } from '@mui/material';
import { DemoContainer } from '@mui/x-date-pickers/internals/demo';
import { AdapterDayjs } from '@mui/x-date-pickers/AdapterDayjs';
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import { DatePicker } from '@mui/x-date-pickers/DatePicker';
import "./CalendarTabPanel.css"
import axios from "axios";
import { API_URL } from "../../consts";
import { useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { TransactionCreateRequestDto } from '../../types/TransactionCreateRequestDto';
import { CategoryResponseDto } from '../../types/CategoryResponseDto';
import dayjs, { Dayjs } from 'dayjs';

const CalendarIncomeTabPanel = () => {
    const [value, setValue] = React.useState(0);
    const { t, i18n } = useTranslation();
    const [category, setCategory] = React.useState('');
    const [categories, setCategories] = React.useState<CategoryResponseDto[]>([]);
    const [name, setName] = React.useState('');
    const [cycle, setCycle] = React.useState('');
    const [period, setPeriod] = React.useState('');
    const [periodType, setPeriodType] = React.useState('');
    const [date, setDate] = React.useState<Dayjs | null>(dayjs());
    const [amount, setAmount] = React.useState(0);
    const { register, handleSubmit, reset } = useForm<TransactionCreateRequestDto>();
    const [validationErrors, setValidationErrors] = React.useState<string[]>([]);
    const [alertHeight, setAlertHeight] = React.useState(0);
    const [openIncomeAddFailedAlert, setOpenIncomeAddFailedAlert] = React.useState(false);
    const [openIncomeAddSuccessAlert, setOpenIncomeAddSuccessAlert] = React.useState(false);

    useEffect(() => {
        featchCategoryData();
    }, []);

    let categoryConfig = {
        method: 'GET',
        url: API_URL + '/groups/all/categories/account/' + localStorage.getItem("id"),
        headers: {
            'Content-Type': 'application/json',
            'Authorization': 'Bearer ' + localStorage.getItem('token')
        },
    };

    const featchCategoryData = async () => {
        axios.request(categoryConfig)
            .then((response) => {
                setCategories(response.data.categories);
            }).catch((error) => {
            })
    }

    const handleCategoryChange = (event: React.ChangeEvent<{ value: unknown }>) => {
        setCategory(event.target.value as string);
        validateIncomeCategory(event.target.value as string);
    };

    const validateIncomeCategory = (category: string) => {
        const isValid = category !== '';
        updateValidationErrors('category', isValid);
        return isValid;
    }

    const handleNameChange = (event: React.ChangeEvent<{ value: unknown }>) => {
        setName(event.target.value as string);
        validateIncomeName(event.target.value as string);
    }

    const validateIncomeName = (name: string) => {
        const isValid = name.length > 3;
        updateValidationErrors('name', isValid);
        return isValid;
    }

    const handleCycleChange = (event: React.ChangeEvent<{ value: unknown }>) => {
        setCycle(event.target.value as string);
        validateIncomeCycle(event.target.value as string);
    }

    const validateIncomeCycle = (cycle: string) => {
        const isValid = (cycle !== ''&& cycle !== 'ONETIME') || cycle === 'ONETIME';
        updateValidationErrors('cycle', isValid);
        return isValid;
    }

    const handlePeriodChange = (event: React.ChangeEvent<{ value: unknown }>) => {
        setPeriod(event.target.value as string);
        validateIncomePeriod(event.target.value as string);
    }

    const validateIncomePeriod = (period: string) => {
        const isValid = period !== '';
        updateValidationErrors('period', isValid);
        return isValid;
    }

    const handlePeriodTypeChange = (event: React.ChangeEvent<{ value: unknown }>) => {
        setPeriodType(event.target.value as string);
        validateIncomePeriodType(event.target.value as string);
    }

    const validateIncomePeriodType = (periodType: string) => {
        const isValid = (periodType !== '' && cycle !== 'ONETIME') || cycle === 'ONETIME';
        updateValidationErrors('periodType', isValid);
        return isValid;
    }

    const handleAmountChange = (event: React.ChangeEvent<{ value: unknown }>) => {
        setAmount(event.target.value as number);
        validateIncomeAmount(event.target.value as number);
    }

    const validateIncomeAmount = (amount: number) => {
        const isValid = amount > 0;
        updateValidationErrors('amount', isValid);
        return isValid;
    }

    const validateIncomeDate = (date: Dayjs | null) => {
        const isValid = date !== null;
        updateValidationErrors('date', isValid);
        return isValid;
    }

    const updateValidationErrors = (field: string, isValid: boolean) => {
        setValidationErrors((prevErrors) => {
            const updatedErrors = { ...prevErrors };
            if (!isValid) {
                switch (field) {
                    case 'category':
                        updatedErrors[field] = t('calendarPanel.categoryValidation');
                        break;
                    case 'name':
                        updatedErrors[field] = t('calendarPanel.nameValidation');
                        break;
                    case 'cycle':
                        updatedErrors[field] = t('calendarPanel.cycleValidation');
                        break;
                    case 'period':
                        updatedErrors[field] = t('calendarPanel.periodValidation');
                        break;
                    case 'periodType':
                        updatedErrors[field] = t('calendarPanel.periodTypeValidation');
                        break;
                    case 'amount':
                        updatedErrors[field] = t('calendarPanel.amountValidation');
                        break;
                    case 'date':
                        updatedErrors[field] = t('calendarPanel.dateValidation');
                        break;      
                    default:
                        break;
                }
            } else {
                delete updatedErrors[field];
            }

            return updatedErrors;
        });
    }

    const handleAlertLoad = (event: React.ChangeEvent<HTMLInputElement>) => {
        setAlertHeight(event.target.clientHeight);
    };


    const onSubmit = handleSubmit((data: TransactionCreateRequestDto) => {
        data.creatorId = localStorage.getItem("id") as string;
        data.categoryId = category;
        data.date = date.toDate().toISOString();
        data.cycle = cycle;
        data.period = period;
        data.periodType = periodType;
        data.value = amount;
        data.name = name;
        data.type = 'INCOME';
        const config = {
            method: 'POST',
            url: API_URL + '/transactions/transaction',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + localStorage.getItem('token')
            },
            data: JSON.stringify(data)
        };
        axios.request(config)
            .then((response) => {
                setCategory('');
                setDate(dayjs());
                setCycle('');
                setPeriod('');
                setPeriodType('');
                setAmount(0);
                setName(''); 

                reset();
                setOpenIncomeAddSuccessAlert(true);
                setOpenIncomeAddFailedAlert(false);
            }).catch((error) => {
                setOpenIncomeAddFailedAlert(true);
                setOpenIncomeAddSuccessAlert(false);
            })
    });

    const buttonTextStyle = {
        textAlign: 'center' as const,
        backgroundColor: 'rgba(131, 225, 180, 1)',
        borderRadius: '5vh',
        fontSize: '1.5rem',
        color: 'rgba(245, 249, 246, 1)',
    };

    const handleIncomeAddFailedAlerttClose = () => {
        setOpenIncomeAddFailedAlert(false);
    }

    const handleIncomeAddSuccessAlertClose = () => {
        setOpenIncomeAddSuccessAlert(false);
    }

    return (
        <div>
        <FormControl>
            <List>
                <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', flexDirection: 'column' }}>
                    <Box component="form" onSubmit={onSubmit}>
                        <ListItem>
                            <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', flexDirection: 'row' }}>
                                <div className="font-link">
                                    {t('calendarPanel.incomeName')}*
                                </div>
                                <div className="paddingConatiner">
                                    <TextField
                                        required
                                        {...register("name")}
                                        id="outlined-required-email"
                                        style={{ backgroundColor: 'white', width: '100%' }}
                                        variant='standard'
                                        value={name}
                                        onChange={handleNameChange}
                                    />
                                </div>
                            </div>
                        </ListItem>

                        <ListItem style={{ display: 'flex' }}>
                            <div className="font-link">
                                {t('calendarPanel.incomeCycle')}*
                            </div>
                            <div>
                                <RadioGroup
                                    row
                                    {...register("cycle")}
                                    value={cycle}
                                    onChange={handleCycleChange}>
                                    <FormControlLabel value={'ONETIME'} control={<Radio />} label={t('calendarPanel.oneTimeIncome')} />
                                    <FormControlLabel value={'CYCLE'} control={<Radio />} label={t('calendarPanel.recurringIncome')} />
                                </RadioGroup>
                            </div>
                        </ListItem>

                        <ListItem style={{ display: 'flex' }}>
                            <div className="font-link">
                                {t('calendarPanel.period')}*
                            </div>
                            <div>
                                <RadioGroup
                                    row
                                    {...register("periodType")}
                                    value={cycle === 'ONETIME' ? "" : periodType}
                                    onChange={handlePeriodTypeChange}
                                >
                                    <div className="paddingConatiner">
                                        <TextField
                                            required
                                            {...register("period")}
                                            id="outlined-required-email"
                                            style={{ backgroundColor: 'white', width: '100%' }}
                                            variant='standard'
                                            value={cycle === 'ONETIME' ? "" : period}
                                            onChange={handlePeriodChange}
                                            disabled={cycle === 'ONETIME'}
                                        />
                                    </div>
                                    <div className="paddingConatiner">
                                    <FormControlLabel value={'DAY'} control={<Radio disabled={cycle === 'ONETIME'} />} label={t('calendarPanel.days')} />
                                    <FormControlLabel value={'MONTH'} control={<Radio disabled={cycle === 'ONETIME'} />} label={t('calendarPanel.months')} />
                                    <FormControlLabel value={'YEAR'} control={<Radio disabled={cycle === 'ONETIME'} />} label={t('calendarPanel.years')} />
                                    </div>
                                </RadioGroup>
                            </div>
                        </ListItem>

                        <ListItem style={{ display: 'flex' }}>
                            <div className="font-link">
                                {t('calendarPanel.category')}*
                            </div>
                            <div className="paddingConatiner">
                                <Select
                                    {...register("categoryId")}
                                    labelId="demo-simple-select-label"
                                    id="demo-simple-select"
                                    value={category}
                                    onChange={handleCategoryChange}
                                >
                                    {categories.map((item) => (
                                        <MenuItem key={item.id} value={item.id}>
                                            {item.name + " " + item.groupName}
                                        </MenuItem>
                                    ))}
                                </Select>
                            </div>
                        </ListItem>

                        <ListItem style={{ display: 'flex' }}>
                            <div className="font-link">
                                {t('calendarPanel.date')}*
                            </div>
                            <div className="paddingConatiner">
                                <LocalizationProvider dateAdapter={AdapterDayjs}>
                                    <DemoContainer components={['DatePicker']}>
                                        <DatePicker
                                            value={date}
                                            onChange={(newDate) =>  {
                                                setDate(newDate)
                                                validateIncomeDate(newDate)}
                                                } />
                                    </DemoContainer>
                                </LocalizationProvider>
                            </div>
                        </ListItem>

                        <ListItem>
                            <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', flexDirection: 'row' }}>
                                <div className="font-link">
                                    {t('calendarPanel.amount')}*
                                </div>
                                <div className="paddingConatiner">
                                    <TextField
                                        required
                                        {...register("value")}
                                        id="outlined-required-email"
                                        style={{ backgroundColor: 'white', width: '100%' }}
                                        variant='standard'
                                        value={amount}
                                        onChange={handleAmountChange}
                                    />
                                </div>
                            </div>
                        </ListItem>


                        <ListItem style={{ display: 'flex', justifyContent: 'right' }}>
                            <ListItemButton onClick={onSubmit} style={{ paddingTop: '3vh' }}>
                                <ListItemText>
                                    <div className="font-link" style={buttonTextStyle}>
                                        {t('calendarPanel.add')}
                                    </div>
                                </ListItemText>
                            </ListItemButton>
                        </ListItem>
                    </Box>
                </div>
            </List>
        </FormControl>

        <Stack sx={{
                    width: '20%',
                    position: 'fixed',
                    bottom: 16 + alertHeight,
                    right: 16,
                    zIndex: 1000,
                }} spacing={2}>
                    {Object.values(validationErrors).map((error, index) => (
                        <Alert key={index} severity="error" onLoad={handleAlertLoad}>
                            <AlertTitle>{t('registerPanel.formValidation')}</AlertTitle>
                            {error}
                        </Alert>
                    ))}
                </Stack>


                <Snackbar open={openIncomeAddFailedAlert} autoHideDuration={6000} onClose={handleIncomeAddFailedAlerttClose}>
                <Alert severity="error" sx={{ width: '100%' }} onClose={handleIncomeAddFailedAlerttClose}>
                    {t('calendarPanel.addIncomeFailed')}
                </Alert>
            </Snackbar>


            <Snackbar open={openIncomeAddSuccessAlert} autoHideDuration={6000} onClose={handleIncomeAddSuccessAlertClose}>
                <Alert severity="success" sx={{ width: '100%' }} onClose={handleIncomeAddSuccessAlertClose}>
                    {t('calendarPanel.addIncomeSuccess')}
                </Alert>
            </Snackbar>

        </div>
    )
}

export default CalendarIncomeTabPanel;