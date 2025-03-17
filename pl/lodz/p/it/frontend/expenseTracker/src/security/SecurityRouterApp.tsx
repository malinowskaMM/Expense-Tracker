import { createBrowserRouter, Outlet, useNavigate } from 'react-router-dom';
import Login from "../components/login/LoginPanel";
import NavbarPanel from "../components/base/NavbarPanel";
import Register from "../components/register/RegistrationPanel";
import React from "react";
import { NONE } from "../consts";
import GroupPanel from '../components/group/GroupPanel';
import CalendarPanel from '../components/calendar/CalendarPanel';
import CategoryPanel from '../components/category/CategoryPanel';
import AnalysisPanel from '../components/analysis/AnalysisPanel';
import ForgotPasswordPanel from '../components/login/ForgotPasswordPanel';
import AccountActivatedPanel from '../components/login/AccountActivatedPanel';
import AccountPanel from '../components/account/AccountPanel';
import AccountEmailChangedPanel from '../components/login/AccountEmailChangedPanel';
import AdminAccountPasswordSetPanel from '../components/login/AdminAccountPasswordSetPanel';
import AdminAccountPasswordSetInitPanel from '../components/login/AdminAccountPasswordSetInitPanel';
import AccountDetailsPanel from '../components/account/AccountDetailsPanel';
import AccountsListPanel from '../components/account/AccountsListPanel';
import AccountAfterRegistrationPanel from '../components/register/AccountAfterRegistrationPanel';
import AccountForgotPasswordSetPanel from '../components/login/AccountForgotPasswordSetPanel';
import BasePanel from '../components/base/BasePanel';
import { GUEST, ADMIN, USER } from "../consts";
import { useEffect } from "react";
import AccountPasswordSetPanel from "../components/account/AccountPasswordSetPanel";
import AccountInitEmailChangedPanel from "../components/login/AccountInitEmailChangedPanel";
import GroupsListPanel from "../components/group/GroupsListPanel";


interface PrivateRouteProps {
    component: React.ComponentType<any>;
    accessLevels: string[];
}

const PrivateRoute: React.FC<PrivateRouteProps> = ({ component: Component, accessLevels, ...rest }) => {
    const userAccessLevel = localStorage.getItem("role");
    const navigate = useNavigate();
    const token = localStorage.getItem("token");


    useEffect(() => {
        if (token === null) {
            localStorage.setItem("role", GUEST);
        }
        if (userAccessLevel !== null && !accessLevels.includes(userAccessLevel)) {
            navigate('/');
        }
    }, [localStorage.getItem("role")]);

    return <Component {...rest} />;
};

const router = createBrowserRouter([
    {
        path: '/',
        element: (<><NavbarPanel /><Outlet /></>),
        children: [
            {
                path: '/login',
                element: <PrivateRoute component={Login} accessLevels={[GUEST]} />
            },
            {
                path: '/register',
                element: <PrivateRoute component={Register} accessLevels={[GUEST]} />
            },
            {
                path: '/success-register',
                element: <PrivateRoute component={AccountAfterRegistrationPanel} accessLevels={[GUEST]} />
            },
            {
                path: '/forgot-password',
                element: <PrivateRoute component={ForgotPasswordPanel} accessLevels={[GUEST]} />
            },
            {
                path: '/set-password/:token',
                element: <PrivateRoute component={AccountForgotPasswordSetPanel} accessLevels={[GUEST]} />
            },
            {
                path: '/success-set-password',
                element: <PrivateRoute component={AccountPasswordSetPanel} accessLevels={[GUEST]} />
            },
            {
                path: '/activate-account/:token',
                element: <PrivateRoute component={AccountActivatedPanel} accessLevels={[GUEST]} />
            },
            {
                path: '/change-email',
                element: <PrivateRoute component={AccountInitEmailChangedPanel} accessLevels={[GUEST]} />
            },
            {
                path: '/success-change-email/:id/:token/:email',
                element: <PrivateRoute component={AccountEmailChangedPanel} accessLevels={[GUEST]} />
            },
            {
                path: '/success-set-admin-password',
                element: <PrivateRoute component={AdminAccountPasswordSetPanel} accessLevels={[GUEST]} />
            },
            {
                path: '/set-admin-password/:token',
                element: <PrivateRoute component={AdminAccountPasswordSetInitPanel} accessLevels={[GUEST]} />
            }, 
            {

                path: '/',
                element: <PrivateRoute component={BasePanel} accessLevels={[GUEST, ADMIN, USER]} />
            },
            {
                path: '/group',
                element: <PrivateRoute component={GroupPanel} accessLevels={[USER]} />
            },
            {
                path: '/calendar',
                element: <PrivateRoute component={CalendarPanel} accessLevels={[USER]} />
            },
            {
                path: '/category',
                element: <PrivateRoute component={CategoryPanel} accessLevels={[USER]} />
            },
            {
                path: '/analysis',
                element: <PrivateRoute component={AnalysisPanel} accessLevels={[USER]} />
            },
            {
                path: '/my-account',
                element: <PrivateRoute component={AccountPanel} accessLevels={[ADMIN, USER]} />
            },
            {
                path: '/accounts',
                element: <PrivateRoute component={AccountsListPanel} accessLevels={[ADMIN]} />,
            },
            {
                path: '/groups',
                element: <PrivateRoute component={GroupsListPanel} accessLevels={[ADMIN]} />,
            },
            {
                path: '/account',
                element: <PrivateRoute component={AccountDetailsPanel} accessLevels={[NONE]} />,
                children: [
                    {
                        path: '/accounts/:accountId',
                        element: <PrivateRoute component={AccountDetailsPanel} accessLevels={[ADMIN]} />
                    }
                ]
            }
        ]
    }
]);
export default router;