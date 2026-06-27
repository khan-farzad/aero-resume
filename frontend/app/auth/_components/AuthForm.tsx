"use client";
import { ChangeEvent, useState } from "react";

type User = {
  email?: string;
  password?: string;
  username?: string;
};
type AuthFormProps = {
  title: string;
  subtitle: string;
  buttonText: string;
  fields: Fields[];
};

const AuthForm = ({ title, subtitle, buttonText, fields }: AuthFormProps) => {
  const [user, setUser] = useState<User>({});
  const handleChange = (e: ChangeEvent<HTMLInputElement>) => {
    const field = e.target.name;
    const value = e.target.value;
    if (field === "username/email") {
      setUser({
        ...user,
        username: value,
        email: value,
      });
    } else {
      setUser({
        ...user,
        [e.target.name]: value,
      });
    }
  };
  const handleSubmit = () => {
    console.log(user);
  };

  return (
    <div className="md:w-1/2 w-full h-full flex items-center flex-col p-4 gap-4 justify-center text-black text-center overflow-scroll">
      <div className="flex flex-col items-center">
        <h2 className="font-semibold text-2xl">{title}</h2>
        <p className="font-extralight text-xs text-black/50">{subtitle}</p>
      </div>
      <div className="*:bg-slate-200  flex flex-col gap-4 w-3/4 justify-evenly">
        {fields.map((f, i) => (
          <input
            key={i}
            name={f.name}
            placeholder={f.placeholder}
            type={f.type}
            onChange={(e) => handleChange(e)}
            className="rounded-md px-4 py-2 placeholder:text-sm"
          ></input>
        ))}
      </div>
      <button
        onClick={handleSubmit}
        className="cursor-pointer px-8 py-2 bg-blue-900 rounded-lg text-white font-medium"
      >
        {buttonText}
      </button>
    </div>
  );
};

export default AuthForm;
