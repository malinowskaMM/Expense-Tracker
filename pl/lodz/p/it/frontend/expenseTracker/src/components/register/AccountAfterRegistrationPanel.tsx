import Box from '@mui/material/Box';
import { useTranslation } from "react-i18next";
import List from '@mui/material/List';
import ListItem from '@mui/material/ListItem';
import { ListItemButton, ListItemText } from '@mui/material';
import "../../i18n";
import { useNavigate } from 'react-router-dom';

const AccountAfterRegistrationPanel = () => {
    const { t, i18n } = useTranslation();
    const logo = require("../../assets/logo.svg").default;
    const navigate = useNavigate();

    const containerStyle = {
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
        height: '100vh',
        width: '100vw',
    };

    const boxStyle = {
        backgroundColor: 'rgba(153, 237, 197, 0.5)',
        display: 'flex',
        flexDirection: 'column',
        justifyContent: 'center',
        alignItems: 'center',
        width: '40%',
        height: '70%',
        borderRadius: '5vh',
    };

    const listStyle = {
        width: '100%',
        aliginItems: 'center',
        justifyContent: 'center'
    };

    const buttonTextStyle = {
        textAlign: 'center' as const,
        backgroundColor: 'rgba(131, 225, 180, 1)',
        borderRadius: '5vh',
        fontSize: '1.5rem',
        color: 'rgba(245, 249, 246, 1)',
    };

    const imageStyle = {
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center'
    };

    const headerTextStyle = {
        textAlign: 'center' as const,
        fontSize: '3.0rem',
        color: 'rgba(43, 42, 42, 0.8)',

    };


    return (
        <div style={containerStyle} className="font-link">
            <Box sx={boxStyle}>
                <div style={imageStyle}>
                    <img src={logo} alt='logo' style={{ width: '25%' }} />
                </div>

                <div>
                    <div style={headerTextStyle}>
                        {t('accountAfterRegistrationPanel.accountAfterRegistration')}
                    </div>


                    <List style={listStyle}>
                        <div style={{ width: '100%', justifyContent: 'center', alignItems: 'center'}}>
                            <ListItem>
                                <div>
                                    {t('accountAfterRegistrationPanel.accountAfterRegistrationDescription')}
                                </div>

                            </ListItem>
                        </div>

                        <ListItem>
                            <ListItemButton onClick = { () => navigate('/')}>
                                <ListItemText>
                                    <div className="font-link" style={buttonTextStyle}>
                                        {t('accountAfterRegistrationPanel.mainPage')}
                                    </div>
                                </ListItemText>
                            </ListItemButton>
                        </ListItem>
                    </List>
                </div>
            </Box>
        </div>
    );
}

export default AccountAfterRegistrationPanel;