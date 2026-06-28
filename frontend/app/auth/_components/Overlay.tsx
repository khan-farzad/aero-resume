"use client";
import { useState } from "react";
import OverlayCard from "./OverlayCard";

const Overlay = () => {
  const [isLogin, setIsLogin] = useState(true);
  return (
    <div
      className={`z-10 absolute top-0 left-0 size-full text-white flex md:flex-row flex-col rounded-4xl transition-all duration-500 overflow-hidden ${
        isLogin
          ? "translate-y-1/2 md:translate-y-0 md:translate-x-1/2"
          : "-translate-y-1/2 md:translate-y-0 md:-translate-x-1/2"
      }`}
    >
      <OverlayCard
        title="Hello, Friend!"
        subtitle="Register with your personal details to use all features"
        buttonText="SIGN UP"
        handleClick={() => setIsLogin(false)}
      />
      <OverlayCard
        title="Welcome Back!"
        subtitle="Enter your personal details to use all features"
        buttonText="SIGN IN"
        handleClick={() => setIsLogin(true)}
      />
    </div>
  );
};

export default Overlay;
